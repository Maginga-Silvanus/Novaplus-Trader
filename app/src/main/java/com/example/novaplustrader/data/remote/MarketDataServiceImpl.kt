package com.example.novaplustrader.data.remote

import android.util.Log
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.domain.model.trading.MarketStatus
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class MarketDataServiceImpl : MarketDataService {
    private val logTag = "MarketDataService"
    private val _connectionState = MutableStateFlow(MarketConnectionState.CONNECTING)
    private val _marketStatus = MutableStateFlow(MarketStatus.DATA_UNAVAILABLE)
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    override fun observeConnectionState(): StateFlow<MarketConnectionState> = _connectionState.asStateFlow()
    override fun observeMarketStatus(asset: String): StateFlow<MarketStatus> = _marketStatus.asStateFlow()

    override fun observeMarketTicks(asset: String): Flow<MarketTick> = flow {
        val symbol = BinanceMarketParser.exchangeSymbol(asset)
        var lastTick: MarketTick? = null
        val reconnect = MarketReconnectTracker()
        while (currentCoroutineContext().isActive) {
            try {
                _connectionState.value = MarketConnectionState.CONNECTING
                val initial = withContext(Dispatchers.IO) { fetchRestTicker(symbol) }
                lastTick = initial
                reconnect.onRestDataReceived()
                emit(initial)
                _connectionState.value = MarketConnectionState.CONNECTED
                _marketStatus.value = MarketStatus.OPEN
                tickerStream(symbol).collect { tick ->
                    lastTick = tick
                    _connectionState.value = reconnect.onConnected()
                    emit(tick)
                }
                throw IOException("Binance ticker stream closed")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(logTag, "Binance ticker connection failed for $symbol", error)
                _connectionState.value = reconnect.onDisconnected()
                _marketStatus.value = MarketStatus.DATA_UNAVAILABLE
                lastTick?.let { emit(it.copy(isStale = true)) }
                delay(reconnect.nextRetryDelay((error as? BinanceRateLimitException)?.retryAfterMillis))
            }
        }
    }

    override fun observeMarkets(): Flow<List<MarketSummary>> = flow {
        var latest = emptyList<MarketSummary>()
        val reconnect = MarketReconnectTracker()
        while (currentCoroutineContext().isActive) {
            try {
                _connectionState.value = MarketConnectionState.CONNECTING
                val availableSymbols = withContext(Dispatchers.IO) { fetchSupportedSpotSymbols() }
                require(availableSymbols.isNotEmpty()) { "No supported Binance spot pairs are currently trading" }
                latest = withContext(Dispatchers.IO) { fetchMarketSummaries(availableSymbols) }
                require(latest.isNotEmpty()) { "Binance returned no market tickers for supported pairs" }
                reconnect.onRestDataReceived()
                emit(latest)
                _connectionState.value = MarketConnectionState.CONNECTED
                _marketStatus.value = MarketStatus.OPEN
                val stream = combinedTickerStream(availableSymbols)
                withTimeout(WEBSOCKET_IDLE_TIMEOUT_MS) {
                    stream.collect { incoming ->
                        latest = latest.associateBy { it.symbol }.toMutableMap().apply {
                            incoming.forEach { put(it.symbol, it) }
                        }.values.sortedBy { it.symbol }
                        emit(latest)
                        _connectionState.value = reconnect.onConnected()
                    }
                }
                throw IOException("Binance market stream closed")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(logTag, "Binance market stream failed", error)
                _connectionState.value = reconnect.onDisconnected()
                _marketStatus.value = MarketStatus.DATA_UNAVAILABLE
                if (latest.isNotEmpty()) emit(latest.map { it.copy(isStale = true) })
                delay(reconnect.nextRetryDelay((error as? BinanceRateLimitException)?.retryAfterMillis))
            }
        }
    }

    override fun observeMarketCandles(asset: String, timeframe: Timeframe): Flow<List<MarketCandle>> = flow {
        val symbol = BinanceMarketParser.exchangeSymbol(asset)
        var latest = emptyList<MarketCandle>()
        val reconnect = MarketReconnectTracker()
        while (currentCoroutineContext().isActive) {
            try {
                _connectionState.value = MarketConnectionState.CONNECTING
                latest = withContext(Dispatchers.IO) { fetchCandles(symbol, asset, timeframe) }
                if (latest.isNotEmpty()) reconnect.onRestDataReceived()
                emit(latest)
                _connectionState.value = MarketConnectionState.CONNECTED
                _marketStatus.value = MarketStatus.OPEN
                val stream = candleStream(symbol, asset, timeframe)
                withTimeout(WEBSOCKET_IDLE_TIMEOUT_MS) {
                    stream.collect { incoming ->
                        latest = BinanceMarketParser.mergeCandles(latest, incoming)
                        emit(latest)
                        _connectionState.value = reconnect.onConnected()
                    }
                }
                throw IOException("Binance kline stream closed")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(logTag, "Binance kline stream failed for $symbol ${timeframe.interval}", error)
                _connectionState.value = reconnect.onDisconnected()
                _marketStatus.value = MarketStatus.DATA_UNAVAILABLE
                delay(reconnect.nextRetryDelay((error as? BinanceRateLimitException)?.retryAfterMillis))
            }
        }
    }

    private suspend fun fetchRestTicker(symbol: String): MarketTick = withContext(Dispatchers.IO) {
        BinanceMarketParser.parseTicker(
            requestText("$REST_BASE/ticker/24hr?symbol=$symbol"),
            symbol
        )
    }

    private suspend fun fetchSupportedSpotSymbols(): Set<String> = withContext(Dispatchers.IO) {
        val symbols = BinanceMarketParser.supportedSymbols.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        ) { "\"$it\"" }
        BinanceMarketParser.parseExchangeInfo(
            requestText("$REST_BASE/exchangeInfo?symbols=${urlEncode(symbols)}")
        )
    }

    private suspend fun fetchMarketSummaries(symbols: Set<String>): List<MarketSummary> =
        withContext(Dispatchers.IO) {
            val parameter = symbols.sorted().joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]"
            ) { "\"$it\"" }
            BinanceMarketParser.parseTickerArray(
                requestText("$REST_BASE/ticker/24hr?symbols=${urlEncode(parameter)}"),
                symbols
            )
        }

    private suspend fun fetchCandles(
        symbol: String,
        asset: String,
        timeframe: Timeframe
    ): List<MarketCandle> = withContext(Dispatchers.IO) {
        BinanceMarketParser.parseRestCandles(
            requestText("$REST_BASE/klines?symbol=$symbol&interval=${timeframe.interval}&limit=100"),
            asset,
            timeframe,
            System.currentTimeMillis()
        )
    }

    private fun tickerStream(symbol: String): Flow<MarketTick> = flow {
        webSocketStream("$WS_BASE/${symbol.lowercase()}@ticker").collect { json ->
            emit(BinanceMarketParser.parseTicker(json, symbol))
        }
    }

    private fun combinedTickerStream(symbols: Set<String>): Flow<List<MarketSummary>> {
        val streams = symbols.sorted().joinToString("/") { "${it.lowercase()}@ticker" }
        return flow {
            webSocketStream("$WS_BASE/stream?streams=$streams").collect { json ->
                val event = JSONObject(json)
                val data = event.optJSONObject("data") ?: event
                val summaries = BinanceMarketParser.parseTickerArray(JSONArray().put(data).toString(), symbols)
                if (summaries.isNotEmpty()) emit(summaries)
            }
        }
    }

    private fun candleStream(symbol: String, asset: String, timeframe: Timeframe): Flow<MarketCandle> =
        flow {
            webSocketStream("$WS_BASE/${symbol.lowercase()}@kline_${timeframe.interval}").collect { json ->
                emit(BinanceMarketParser.parseWebSocketCandle(json, asset, timeframe))
            }
        }

    private fun webSocketStream(endpoint: String): Flow<String> = callbackFlow {
        val request = Request.Builder().url(endpoint).build()
        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val result = trySend(text)
                if (result.isFailure) {
                    Log.w(logTag, "Dropped Binance market event because the collector is closed")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close(IOException("Binance WebSocket closed ($code): $reason"))
            }
        })
        awaitClose { webSocket.cancel() }
    }

    private fun requestText(endpoint: String): String {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = HTTP_TIMEOUT_MS
        connection.readTimeout = HTTP_TIMEOUT_MS
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        return try {
            val statusCode = connection.responseCode
            val body = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (statusCode == 429 || statusCode == 418) {
                val retryAfterSeconds = connection.getHeaderField("Retry-After")?.toLongOrNull()
                throw BinanceRateLimitException(
                    retryAfterMillis = retryAfterSeconds?.times(1_000L),
                    message = "Binance rate limit reached (HTTP $statusCode)"
                )
            }
            check(statusCode in 200..299) {
                "Market data request failed (HTTP $statusCode): ${body.take(200)}"
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private fun urlEncode(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name())

    private class BinanceRateLimitException(
        val retryAfterMillis: Long?,
        message: String
    ) : IOException(message)

    private companion object {
        const val REST_BASE = "https://api.binance.com/api/v3"
        const val WS_BASE = "wss://stream.binance.com:9443"
        const val HTTP_TIMEOUT_MS = 10_000
        const val WEBSOCKET_IDLE_TIMEOUT_MS = 30_000L
    }
}

class MarketReconnectTracker {
    private var hasReceivedData = false
    private var backoffMs = BinanceRetryPolicy.INITIAL_BACKOFF_MS

    fun onRestDataReceived() {
        hasReceivedData = true
    }

    fun onDataReceived() {
        hasReceivedData = true
        backoffMs = BinanceRetryPolicy.INITIAL_BACKOFF_MS
    }

    fun onConnected(): MarketConnectionState {
        onDataReceived()
        return MarketConnectionState.CONNECTED
    }

    fun onDisconnected(): MarketConnectionState =
        if (hasReceivedData) MarketConnectionState.STALE else MarketConnectionState.DISCONNECTED

    fun nextRetryDelay(retryAfterMs: Long? = null): Long {
        val delayMs = BinanceRetryPolicy.delayMillis(backoffMs, retryAfterMs)
        backoffMs = BinanceRetryPolicy.nextBackoffMillis(backoffMs)
        return delayMs
    }
}

object BinanceRetryPolicy {
    const val INITIAL_BACKOFF_MS = 1_000L
    const val MAX_BACKOFF_MS = 30_000L
    const val MAX_RETRY_AFTER_MS = 300_000L

    fun nextBackoffMillis(currentMs: Long): Long =
        (currentMs * 2).coerceIn(INITIAL_BACKOFF_MS, MAX_BACKOFF_MS)

    fun delayMillis(backoffMs: Long, retryAfterMillis: Long? = null): Long {
        val safeBackoff = backoffMs.coerceIn(INITIAL_BACKOFF_MS, MAX_BACKOFF_MS)
        val providerDelay = retryAfterMillis ?: return safeBackoff
        return providerDelay.coerceIn(safeBackoff, MAX_RETRY_AFTER_MS)
    }
}
