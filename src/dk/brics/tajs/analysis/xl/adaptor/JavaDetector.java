package dk.brics.tajs.analysis.xl.adaptor;

import dk.brics.tajs.analysis.xl.translator.LocalJavaTranslatorCopy;
import dk.brics.tajs.analysis.js.NodeTransfer;
import dk.brics.tajs.flowgraph.SourceLocation;
import dk.brics.tajs.flowgraph.jsnodes.CallNode;
import dk.brics.tajs.flowgraph.jsnodes.JavaNode;
import dk.brics.tajs.flowgraph.jsnodes.Node;
import dk.brics.tajs.flowgraph.jsnodes.ReadPropertyNode;
import dk.brics.tajs.flowgraph.jsnodes.ReadVariableNode;
import dk.brics.tajs.lattice.ObjectLabel;
import dk.brics.tajs.lattice.State;
import dk.brics.tajs.lattice.Value;

public class JavaDetector extends NodeTransfer {

    /**
     * Constructs a new TransferFunctions object.
     */
    public JavaDetector() {}

    @Override
    public void visit(ReadVariableNode n) {
        if(n.getVariableName().equals("Java")) {

            //URL url = new URL("file", null, "");
            SourceLocation sl = new SourceLocation.StaticLocationMaker(null).make(0, 0, 1, 1);
            JavaNode javaNode = new JavaNode(sl, Long.valueOf(1)); // JavaNode(sl, 1);
            javaNode.setIndex(1);
            ObjectLabel objectLabel = ObjectLabel.make(javaNode, ObjectLabel.Kind.JAVAOBJECT);
            objectLabel.javaName = "Java";
            Value javaObject = Value.makeObject(objectLabel);
            writeToRegisterAndAddMustReachDefs(n.getResultRegister(), javaObject, n);
        }
        else
            super.visit(n);
    }

    @Override
    public void visit(ReadPropertyNode n) {
        String propertyName = n.getPropertyString();
        int baseRegister = n.getBaseRegister();
        if(propertyName!=null && propertyName.equals("type") && c.getState().readRegister(baseRegister).getStr().equals("Java")) {
            //do nothing
        }
        else {
            Value baseValue = c.getState().readRegister(baseRegister);
            if (baseValue.isJavaObject()) {
                if(n.getResultRegister()>0) {
                    String javaFullClassName = baseValue.getJavaName();
                    Value v = LocalJavaTranslatorCopy.getLocalJavaTranslatorCopy().
                            queryPropertyValue(javaFullClassName, propertyName);
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                }
                return ;
            }
        }
        super.visit(n);
    }

    @Override
    public void visit(CallNode n) {
       int baseRegister = n.getBaseRegister();
        String functionName = n.getPropertyString();
        if (baseRegister > 0) {
            Value baseValue = c.getState().readRegister(baseRegister);
        if(n.getNumberOfArgs()>0) {
            int argumentRegister = n.getArgRegister(0);
                if (baseValue.isJavaObject() && functionName.equals("type")) {
                    String javaType = c.getState().readRegister(argumentRegister).getStr();
                    ObjectLabel.Kind jol = ObjectLabel.Kind.JAVAOBJECT;
                    ObjectLabel ol =  ObjectLabel.make(n, jol);
                    ol.javaName = javaType;
                    Value v = Value.makeObject(ol).setDontDelete().setDontEnum().setReadOnly();
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n); //Value.makeStr(javaObjectConst + javaType)
                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    return;
                }
        }
            if (baseValue.isJavaObject()) {
                // n.getResultRegister()=-1 -> the node is no assignment
                if(n.getResultRegister()>0){
                    String javaFullClassName = baseValue.getJavaName();
                    Value v = LocalJavaTranslatorCopy.getLocalJavaTranslatorCopy().
                            queryFunctionValue(javaFullClassName, functionName);
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                }
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

