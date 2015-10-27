package org.dsa.iot.relayr.actions;

import org.dsa.iot.commons.ParameterizedAction;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.relayr.relayr.Relayr;

import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class EditAction extends ParameterizedAction {

    private EditAction() {
        super(Permission.WRITE);
    }

    @Override
    public void handle(ActionResult result,
                       Map<String, Value> params) {
        Value token = params.get("Token");
        Node node = result.getNode();
        node.setConfig("token", token);
        Relayr relayr = node.getMetaData();
        relayr.init();
    }

    public static Node init(Node parent) {
        NodeBuilder b = parent.createChild("Edit");
        {
            ParameterizedAction a = new EditAction();
            {
                ParameterInfo p = new ParameterInfo("Token", ValueType.STRING);
                p.setPersistent(true);
                {
                    Node child = parent.getChild("Edit");
                    if (child != null) {
                        Value v = child.getConfig("token");
                        if (v != null) {
                            p.setDefaultValue(v);
                        }
                    }
                }
                a.addParameter(p);
            }
            b.setAction(a);
        }
        return b.build();
    }
}
