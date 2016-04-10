package org.anneem23.metal.cat.raumfeld;

import org.anneem23.metal.cat.body.MetalCallback;
import org.apache.commons.io.FileUtils;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.*;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.lastchange.Event;
import org.fourthline.cling.support.lastchange.EventedValue;
import org.fourthline.cling.support.lastchange.InstanceID;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class MetalListener implements Runnable {

    public static final UDAServiceId AV_TRANSPORT = new UDAServiceId("AVTransport");
    public static final DeviceType MEDIA_RENDERER = new DeviceType("schemas-upnp-org", "MediaRenderer");
    public static final String LAST_CHANGE = "LastChange";

    private final AVTransportStateChangeParser parser = new AVTransportStateChangeParser();
    private final MetalCallback metalCallback;

    public MetalListener(MetalCallback callback) {
        this.metalCallback = callback;
    }


    public void run() {
        try {

            UpnpService upnpService = new UpnpServiceImpl();

            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );

            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

    RegistryListener createRegistryListener(final UpnpService upnpService) {
        return new DefaultRegistryListener() {

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                if (MEDIA_RENDERER.equals(device.getType())) {
                    for (Service service : device.getServices()) {

                        if (AV_TRANSPORT.equals(service.getServiceId())) {
                            SubscriptionCallback callback = getSubscriptionCallback(service);

                            upnpService.getControlPoint().execute(callback);
                        }
                    }
                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                /*Service switchPower;
                if ((switchPower = device.findService(AV_TRANSPORT)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                }*/
            }

        };
    }

    private SubscriptionCallback getSubscriptionCallback(final Service service) {
        return new SubscriptionCallback(service, 600) {

            @Override
            public void established(GENASubscription sub) {
                System.out.println("Established: " + sub.getSubscriptionId());
            }

            @Override
            protected void failed(GENASubscription subscription,
                                  UpnpResponse responseStatus,
                                  Exception exception,
                                  String defaultMsg) {
                System.err.println(defaultMsg);
            }

            @Override
            public void ended(GENASubscription sub,
                              CancelReason reason,
                              UpnpResponse response) {
            }

            @Override
            public void eventReceived(GENASubscription sub) {
                Map<String, StateVariableValue> currentValues = sub.getCurrentValues();
                if (currentValues.containsKey(LAST_CHANGE)) {
                    StateVariableValue value = currentValues.get(LAST_CHANGE);
                    try {
                        String xml = value.getValue().toString();
                        Event event = parser.parse(xml);
                        for (InstanceID instanceID : event.getInstanceIDs()) {

                            for (EventedValue eventedValue : instanceID.getValues()) {

                                /*if ("AVTransportURIMetaData".equals(eventedValue.getName())) {
                                    DIDLContent content = MetaDataParser.parse((String) eventedValue.getValue());
                                }*/
                                if ("AVTransportURI".equals(eventedValue.getName())) {
                                    //FileUtils.copyURLToFile(new URL(eventedValue.getName()), new File("/tmp/"));
                                    metalCallback.dance((URI) eventedValue.getValue());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void eventsMissed(GENASubscription sub, int numberOfMissedEvents) {
                System.out.println("Missed events: " + numberOfMissedEvents);
            }

            @Override
            protected void invalidMessage(RemoteGENASubscription sub,
                                          UnsupportedDataException ex) {
                // Log/send an error report?
            }
        };
    }


    // TODO execute GetPositionInfo call here
    void executeAction(UpnpService upnpService, Service switchPowerService) {

        ActionInvocation setTargetInvocation =
                new SetTargetActionInvocation(switchPowerService);

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        assert invocation.getOutput().length == 0;
                        System.out.println("Successfully called action!");
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                }
        );

    }

    class SetTargetActionInvocation extends ActionInvocation {

        SetTargetActionInvocation(Service service) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", true);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }
}
