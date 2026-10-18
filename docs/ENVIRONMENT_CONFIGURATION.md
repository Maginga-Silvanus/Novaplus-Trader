# Environment Configuration — NovaPlus+ Trader

## Environments
1. **Development**:
   - Local Firebase Emulator Suite or sandbox project.
   - Initial test funding and paper-trading limits configured via environment variables.
2. **Staging / Testing**:
   - Dedicated staging Firebase project.
   - Connected to sandbox payment and market test endpoints.
3. **Production**:
   - Company-owned production Firebase project.
   - Strict security rules, production signing keys, and authorized payment/sports provider integrations.

## Variant Control
- Environment switching and endpoints are governed by Firebase configuration files (`google-services.json`) and secure backend Cloud Functions environment variables.
