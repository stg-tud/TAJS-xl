package dk.brics.tajs.analysis.axa.adaptor;

import dk.brics.tajs.analysis.axa.connector.Connector;
import dk.brics.tajs.analysis.js.NodeTransfer;
import dk.brics.tajs.flowgraph.AbstractNode;
import dk.brics.tajs.flowgraph.SourceLocation;
import dk.brics.tajs.flowgraph.jsnodes.CallNode;
import dk.brics.tajs.flowgraph.jsnodes.JavaNode;
import dk.brics.tajs.flowgraph.jsnodes.Node;
import dk.brics.tajs.flowgraph.jsnodes.ReadPropertyNode;
import dk.brics.tajs.flowgraph.jsnodes.ReadVariableNode;
import dk.brics.tajs.lattice.ObjectLabel;
import dk.brics.tajs.lattice.State;
import dk.brics.tajs.lattice.Value;

import java.net.MalformedURLException;
import java.net.URL;

import static dk.brics.tajs.flowgraph.HostEnvSources.PROTOCOL_NAME;

public class JavaNodeTransfer extends NodeTransfer {

    //final String javaObjectConst = "JavaObject_";

    /**
     * Constructs a new TransferFunctions object.
     */
    public JavaNodeTransfer() {}

    @Override
    public void visit(ReadVariableNode n) {
        if(n.getVariableName().equals("Java")) {
            writeToRegisterAndAddMustReachDefs(n.getResultRegister(), Value.makeStr("Java"), n);
        }
        else
            super.visit(n);
    }

    @Override
    public void visit(ReadPropertyNode n) {
        String propertyName = n.getPropertyString();
        int baseRegister = n.getBaseRegister();
        if(propertyName.equals("type") && c.getState().readRegister(baseRegister).getStr().equals("Java")) {
            //do nothing
        }
        else {
            Value baseValue = c.getState().readRegister(baseRegister);
            if (baseValue.isJavaObject()) {//.getStr().startsWith(javaObjectConst)) {
                if(n.getResultRegister()>0) {
                    String javaFullClassName = baseValue.getStr().substring(11);
                    System.out.println("query: " + javaFullClassName + " : " + propertyName);
                    Value v = Connector.queryPropertyValue(javaFullClassName, propertyName);
                    //TODO request OPAL
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                }
            }
            else
                super.visit(n);
        }
    }

    @Override
    public void visit(CallNode n) {
        int baseRegister = n.getBaseRegister();
        String functionName = n.getPropertyString();
        if (baseRegister > 0) {
            Value baseValue = c.getState().readRegister(baseRegister);
        if(n.getNumberOfArgs()>0) {
            int argumentRegister = n.getArgRegister(0);
                if (baseValue.isStrIdentifier() && baseValue.getStr().equals("Java") && functionName.equals("type")) {
                    String javaType = c.getState().readRegister(argumentRegister).getStr();
                   // try {
                        //URL url = new URL(PROTOCOL_NAME, null, "");
                        //SourceLocation sl = new SourceLocation.StaticLocationMaker(url).make(0, 0, 1, 1);
                        ObjectLabel.Kind jol = ObjectLabel.Kind.JAVAOBJECT;
                        //AbstractNode javaNode = new JavaNode(sl, Long.valueOf(0));
                        //javaNode.setIndex(1000);
                                ObjectLabel ol =  ObjectLabel.make(n, jol);
                                ol.javaName = javaType;
                        Value v = Value.makeObject(ol);
                        Value v2 = v.setDontDelete().setDontEnum().setReadOnly();

                        writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v2, n); //Value.makeStr(javaObjectConst + javaType)
                        State newState = c.getState().clone();
                        c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                   // } catch (MalformedURLException e){}
                    return;
                }
        }
            if (baseValue.isJavaObject()) {
                //TODO request OPAL
                String javaFullClassName = baseValue.getJavaName(); //.getStr().substring(11);
                System.out.println("query: " + javaFullClassName + " : " + functionName);
                Value v = Connector.queryFunctionValue(javaFullClassName, functionName);
                writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                State newState = c.getState().clone();
                c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                return;
            }
        }
        super.visit(n);
    }

    public void visit(JavaNode n){}

    private void writeToRegisterAndAddMustReachDefs(int register, Value v, Node n){
        c.getState().writeRegister(register, v);
        c.getState().getMustReachingDefs().addReachingDef(register, n);
    }
}

