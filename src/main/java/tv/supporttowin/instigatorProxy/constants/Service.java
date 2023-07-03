package tv.supporttowin.instigatorProxy.constants;

public enum Service {
    INSTIGATOR("instigator");


    private final String service;

    Service(final String service) {
        this.service = service;
    }

    public String getService() {
        return this.service;
    }

    @Override
    public String toString() {
        return service;
    }
}
