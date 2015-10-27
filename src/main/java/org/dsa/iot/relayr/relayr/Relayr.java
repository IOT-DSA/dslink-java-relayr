package org.dsa.iot.relayr.relayr;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.User;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.relayr.actions.EditAction;
import rx.Observer;

/**
 * @author Samuel Grenier
 */
public class Relayr {

    private final Node relayrNode;
    private final Node settings;

    private Relayr(Node node, Node settings) {
        this.relayrNode = node;
        this.settings = settings;
        node.setMetaData(this);
        settings.setMetaData(this);
    }

    public void init() {
        Value v = settings.getConfig("token");
        if (v != null) {
            RelayrJavaSdk.Builder b = new RelayrJavaSdk.Builder();
            b.setToken("Bearer " + v.getString());
            b.build();
            for (Node node : relayrNode.getChildren().values()) {
                if (node.getAction() != null) {
                    continue;
                }
                node.delete();
            }
            buildTree();
        }
    }

    private void buildTree() {
        RelayrJavaSdk.getUser().subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(User user) {
                DeviceGroupManager dgm = new DeviceGroupManager(relayrNode, user);
                dgm.init();
            }
        });
    }

    public static Relayr create(Node node) {
        Node settings = EditAction.init(node);
        return new Relayr(node, settings);
    }
}
