package desmoj.core.simulator;

/**
 * Derive from this class to create a conditional which the model as whole
 * may fulfill or not, e.g. for the purpose of conditionally stopping an 
 * experiment. Override method check(E e) such that it returns 
 * <code>true</code> whenever the model complies to the condition.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
public abstract class ModelCondition extends ModelComponent {
	
    /**
     * Stores the arguments of a Constructor-call to make a recreation
     * of the Condition possible.
     */
    private Object[] _arguments = null;
        
    /**
     * Stores if all arguments of a Constructor-call are of primitive
     * types or Strings.
     */
    private boolean _argumentsPrimitive;
        
    /**
     * Constructs a ModelCondition with the given name and parameters for trace
     * files.
     * 
     * @param owner
     *            Model : The main model this condition is associated to
     * @param name
     *            java.lang.String : The name of this condition
     * @param showInTrace
     *            boolean : Flag for showing this condition in trace-files. Set
     *            it to <code>true</code> if model should show up in trace,
     *            <code>false</code> if model should not be shown in trace.
     * @param args
     *            Object... : Arguments to pass to the condition (can be omitted)            
     *            
     */
    public ModelCondition(Model owner, String name, boolean showInTrace, Object... args) {

        super(owner, name, showInTrace); // create a ModelComponent
        
        Class<?>[] types = new Class[3 + args.length];
        types[0] = Model.class;
        types[1] = String.class;
        types[2] = Boolean.TYPE;
                
        _arguments = new Object[3 + args.length];
        _arguments[0] = owner;
        _arguments[1] = name;
        _arguments[2] = showInTrace;
            
        _argumentsPrimitive = true;
            
        for (int i = 0; i < args.length; i++) {
                    
            Class<?> type = getType(args[i]);
            _argumentsPrimitive &= (type != null);
            types[i + 3] = type;
            _arguments[i + 3] = args[i];
        }

    }
    
    /**
     * Returns a boolean showing whether the model complies to the
     * condition tested by this method or not. Inherit from this class and
     * implement this abstract method to return true if the model
     * conforms to your special condition. 
     * 
     * @return boolean : Is <code>true</code>, if the model conforms
     *         to the condition, <code>false</code> otherwise.
     */
    public abstract boolean check();
      
    //TODO Javadoc
    public Object[] getConstructArguments()
    {
        return _arguments;
    }

    //TODO Javadoc
    public boolean hasPrimitiveArguments()
    {
        return _argumentsPrimitive;
    } 
        
    private Class<?> getType(Object obj)
    {
        Class<?> cls = obj.getClass();
        
        return cls.equals(Boolean.class) ? Boolean.TYPE :
            cls.equals(Integer.class) ? Integer.TYPE :
            cls.equals(Character.class) ? Character.TYPE :
            cls.equals(Byte.class) ? Byte.TYPE :
            cls.equals(Short.class) ? Short.TYPE :
            cls.equals(Double.class) ? Double.TYPE :
            cls.equals(Long.class) ? Long.TYPE :
            cls.equals(Float.class) ? Float.TYPE :
            cls.equals(String.class) ? String.class :
            null;
    }
}