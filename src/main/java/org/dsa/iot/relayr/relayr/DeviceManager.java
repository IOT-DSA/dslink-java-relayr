package org.dsa.iot.relayr.relayr;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.Device;
import io.relayr.java.model.action.Reading;
import io.relayr.java.model.models.DeviceFirmware;
import io.relayr.java.model.models.error.DeviceModelsException;
import io.relayr.java.model.models.schema.ValueSchema;
import io.relayr.java.model.models.transport.DeviceReading;
import io.relayr.java.model.models.transport.Transport;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.node.value.ValueUtils;
import org.dsa.iot.dslink.util.Objects;
import rx.Observer;

import java.util.concurrent.TimeUnit;

/**
 * @author Samuel Grenier
 */
public class DeviceManager {

    private final Node node;
    private final Device device;

    public DeviceManager(Node node, Device device) {
        this.node = node;
        this.device = device;
    }

    public void init() {
        if (RelayrJavaSdk.getDeviceModelsCache().isLoading()) {
            Objects.getDaemonThreadPool().schedule(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 200, TimeUnit.MILLISECONDS);
            return;
        }
        device.subscribeToCloudReadings().subscribe(new Observer<Reading>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Reading reading) {
                try {
                    handleReading(reading);
                } catch (DeviceModelsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handleReading(Reading reading) throws DeviceModelsException {
        NodeBuilder b = node.createChild(reading.meaning);
        b.setSerializable(false);
        b.setValueType(ValueType.DYNAMIC);
        ValueSchema schema = getSchema(reading.meaning, reading.path);
        if (schema != null) {
            String unit = schema.getUnit();
            if (unit != null) {
                b.setAttribute("unit", new Value(unit));
            }
        }
        Node node = b.build();
        node.setValue(ValueUtils.toValue(reading.value));
    }

    public ValueSchema getSchema(String meaning,
                                 String path) throws DeviceModelsException {
        DeviceFirmware firmware = device.getDeviceModel().getLatestFirmware();
        Transport transport = firmware.getDefaultTransport();
        for (DeviceReading reading : transport.getReadings()) {
            if (reading.getMeaning().equals(meaning) &&
                    ((path != null && path.equals(reading.getPath())) ||
                            (path == null && reading.getPath() == null))) {
                return reading.getValueSchema();
            }
        }

        return null;
    }
}
