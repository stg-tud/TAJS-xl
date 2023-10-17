package dk.brics.tajs.analysis.xl.adaptor;

import dk.brics.tajs.analysis.xl.translator.LocalTAJSAdapter;
import dk.brics.tajs.analysis.js.NodeTransfer;
import dk.brics.tajs.flowgraph.SourceLocation;
import dk.brics.tajs.flowgraph.jsnodes.*;
import dk.brics.tajs.lattice.ObjectLabel;
import dk.brics.tajs.lattice.State;
import dk.brics.tajs.lattice.Value;

import java.util.Set;
import java.util.stream.Collectors;

public class JavaDetector extends NodeTransfer {

    /**
     * Constructs a new JavaDetector object.
     */
    public JavaDetector() {}

    @Override
    public void visit(ConstantNode n){
      //  System.out.println(n);
      //  System.out.println(n.getBlock().getSingleSuccessor());
        super.visit(n);
    }

    @Override
    public void visit(ReadVariableNode n) {
        if(n.getVariableName().equals("Java")) {

            //URL url = new URL("file", null, "");
            SourceLocation sl = new SourceLocation.StaticLocationMaker(null).make(0, 0, 1, 1);
            JavaNode javaNode = new JavaNode(sl, Long.valueOf(1));
            javaNode.setIndex(1);
            ObjectLabel objectLabel = ObjectLabel.make(javaNode, ObjectLabel.Kind.JAVAOBJECT);
           // objectLabel.javaName = "Java";
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
        Value getBaseValue = c.getState().getRegisters().get(n.getBaseRegister());
        if(getBaseValue.isJavaObject() && n.getResultRegister()==-1){
            return;
        }else if(propertyName!=null && propertyName.equals("type") && c.getState().readRegister(baseRegister).isJavaObject())
            return;
        else {
            Value baseValue = c.getState().readRegister(baseRegister);
            if (baseValue.isJavaObject()) {
                if(n.getResultRegister()>0) {
                    String javaFullClassName = baseValue.getJavaName();
                    //TODO
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), Value.makeUndef(), n);
                }
                return ;
            }
        }
        super.visit(n);
    }

    @Override
    public void visit(WritePropertyNode n){
        String propertyName = n.getPropertyString();
        int baseRegister = n.getBaseRegister();
        Value baseValue = c.getState().readRegister(baseRegister);
        if(baseValue.isJavaObject()){
         //   System.out.println("xxxxxxx----");//"write property: "+n);
            //TODO
       ///     m.visitPropertyAccess(n, baseValue);
            ///     Value coercedBaseval = Conversion.toObject(n, baseValue, c); // models exception if null/undefined
            ///     Set<ObjectLabel> objlabels = coercedBaseval.getObjectLabels();
            ///     Value propertyval = Value.makeStr(n.getPropertyString());
            ///     ParallelTransfer pt = new ParallelTransfer(c);
            ///     Set<Value> propertyvalues = singleton(propertyval);
            //m.visitPropertyWrite(n, objlabels, propertystr);
            ///  baseValue.makeSetter();
           // m.visitVariableOrProperty(n, n.getPropertyString(), n.getSourceLocation(), v, c.getState().getContext(), c.getState());
            ///     pt.complete();
                //TODO
            }
            else
                super.visit(n);
    }

    @Override
    public void visit(CallNode n) {
       int baseRegister = n.getBaseRegister();
        String functionName = n.getPropertyString();

        if(n.isConstructorCall()){
            int functionRegister = n.getFunctionRegister();
            Value functionValue = c.getState().readRegister(functionRegister);
            if(functionValue.isJavaObject()) {
                String javaType = "Test";//c.getState().readRegister(argumentRegister).getStr();
                ObjectLabel.Kind jol = ObjectLabel.Kind.JAVAOBJECT;
                ObjectLabel ol =  ObjectLabel.make(n, jol);
                ol.setJavaName(javaType);
                //Set<ObjectLabel> obls = functionValue.getObjectLabels().stream().map(ol-> ol.cloneWithNode(n)).collect(Collectors.toSet());
                //obls.stream().forEach(obl -> obl.setNode(n));
                Value v = Value.makeObject(ol).setDontDelete().setDontEnum().setReadOnly();


                writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v,
                        n
                ); //Value.makeStr(javaObjectConst + javaType)
            }
            State newState = c.getState().clone();
            c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
        } else if (baseRegister > 0) {
            Value baseValue = c.getState().readRegister(baseRegister);
        if(n.getNumberOfArgs()>0) {
            int argumentRegister = n.getArgRegister(0);
                if (baseValue.isJavaObject() && functionName.equals("type")) {
                    String javaType = c.getState().readRegister(argumentRegister).getStr();
                    ObjectLabel.Kind jol = ObjectLabel.Kind.JAVAOBJECT;
                    ObjectLabel ol =  ObjectLabel.make(n, jol);
                    ol.setJavaName(javaType);
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
                    if(baseValue.getObjectLabels().stream().findFirst().get().getNode().getIndex()>0){
                        try {
                            Value v = LocalTAJSAdapter.getLocalTajsAdapter().queryObject(baseValue); //TODO
                            writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                        }
                        catch (Exception e) {
                            System.out.println("crash");
                        }
                    }
                    else
                        writeToRegisterAndAddMustReachDefs(n.getResultRegister(), Value.makeUndef(), n);

                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    return;
                } else {
                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    return;
                }
            }
        }
        super.visit(n);
    }

    public void visit(JavaNode n){
        //System.out.println("visit Java node");
    }

    public void visit(WriteVariableNode n){
        String variableName = n.getVariableName();
        Value value = c.getState().readRegister(n.getValueRegister());
        if(value.isJavaObject()){

           System.out.println("write variable: "+ n);
        }
        super.visit(n);
    }

    private void writeToRegisterAndAddMustReachDefs(int register, Value v, Node n){
        c.getState().writeRegister(register, v);
        c.getState().getMustReachingDefs().addReachingDef(register, n);
    }
}

