package org.dsa.iot.relayr.relayr;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.Device;
import io.relayr.java.model.User;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.util.Objects;
import rx.Observer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Samuel Grenier
 */
public class DeviceGroupManager {

    private final Node node;
    private final User user;

    public DeviceGroupManager(Node node, User user) {
        this.node = node;
        this.user = user;
    }

    public void init() {
        if (RelayrJavaSdk.getDeviceModelsCache().isLoading()) {
            Objects.getDaemonThreadPool().schedule(new Runnable() {
                @Override
                public void run() {
                    init();
                }
            }, 500, TimeUnit.MILLISECONDS);
            return;
        }

        user.getDevices().subscribe(new Observer<List<Device>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(List<Device> devices) {
                for (Device device : devices) {
                    NodeBuilder b = node.createChild(device.getName());
                    b.setSerializable(false);
                    DeviceManager dm = new DeviceManager(b.build(), device);
                    dm.init();
                }
            }
        });
    }
}
