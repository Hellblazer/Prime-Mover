package desmoj.core.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;

import desmoj.core.report.ModelReporter;
import desmoj.core.report.ReportManager;
import desmoj.core.report.Reporter;

/**
 * The model is supposed to carry references to all static modelcomponents
 * associated to a model. These are modelcomponents like distributions and
 * statistic counters. Note that placing all essential components inside the
 * model class allows you to use object orientation to reuse this model to
 * implement different versions of it using subclassing for changes necessary
 * for using other modelling paradigms. This way models can be easily
 * reimplemented following the process-, event-, transaction- or activity-
 * oriented paradigm. Note that transaction- and activity-oriented modelling
 * requires the higher process synchronization mechanisms (Res, Bin, WaitQueue,
 * CondQueue) by Soenke Claassen.
 * <p>
 * The modelcomponents belonging to a model have to be instantiated in the
 * <code>init()</code> method. They must not be instantiated in the model's
 * constructor since most components need access to a valid experiment object.
 * This can only be accessed after a model has been connected to an experiment
 * using method <code>connectToExperiment(Experiment exp)</code> which
 * implicitly calls the <code>init()</code> method after receiving the valid
 * reference to an experiment.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public abstract class Model extends Reportable {

    /**
     * True if this is the main model (not a submodel to another model), false for
     * submodels.
     */
    boolean isMainModel;

    /**
     * The map with all entities and their identifiers.
     */
    private ReferenceMap _entityMap;

    /**
     * The experiment this model is connected to.
     */
    private Experiment _myExperiment;

    /**
     * The current number of the created entities.
     */
    private long _numberOfEntitiesCreated;

    /**
     * The parameter manager
     */
    private ModelParameterManager _paramManager;

    /**
     * Dynamic List containing all reportable components of this model.
     */
    private ArrayList<Reportable> _reportables;

    /**
     * Dynamic List containing all associated submodels.
     */
    private ArrayList<Model> _subModels;

    /**
     * Constructs a model, with the given name and parameters for report and trace
     * files.
     * 
     * @param name        java.lang.String : The name of this model
     * @param owner       Model : The main model this model is associated to
     * @param showInTrace boolean : Flag for showing this model in trace-files. Set
     *                    it to <code>true</code> if model should show up in trace,
     *                    <code>false</code> if model should not be shown in trace.
     */
    public Model(Model owner, String name, boolean showInReport, boolean showInTrace) {

        super(owner, name, showInReport, showInTrace); // create a reportable

        // init data structures
        _subModels = new ArrayList<Model>(); // create empty Vector for subModels
        _reportables = new ArrayList<Reportable>(); // create empty Vector for reportables

        if (owner != null) { // check if this is a submodel
            this.isMainModel = false;
            owner.registerSubModel(this);
            // register as submodel at owner model
        } else {
            this.isMainModel = true;
        }

        _paramManager = new ParameterManager(); // create ParameterManager

        _numberOfEntitiesCreated = 0L;

        _entityMap = new ReferenceMap(ReferenceStrength.HARD, ReferenceStrength.WEAK);
    }

    /**
     * Connects this model to an experiment. The given experiment must not already
     * be connected to some other model. Otherwise an errormessage will be given and
     * the experiment will be stopped. Since a model that is not yet connected has
     * no access to the experiment's messaging subsystem,
     */
    public void connectToExperiment(Experiment exp) {

        if (exp == null) {
            sendWarning("Can not connect to a <code>null</code> Experiment! " + "Command ignored.",
                        "Model : " + getName() + " Method: void connectToExperiment" + "(Experiment exp)",
                        "The experiment reference passed is a null reference!",
                        "Make sure to use valid references only.");
            return; // no connection possible.
        }

        if (!isMainModel()) { // only main model should register at experiment
            sendWarning("Can not connect to Experiment " + exp.getName() + "! " + "Command ignored.",
                        "Model : " + getName() + " Method: void connectToExperiment" + "(Experiment exp)",
                        "The model to be connected is already submodel to model : " + getModel().getName(),
                        "Only ,main models are allowed to be connected to an " + "experiment");
            return; // model is no main model
        }

        if (exp.isConnected()) {
            sendWarning("Can not connect to Experiment " + exp.getName() + "! " + "Command ignored.",
                        "Model : " + getName() + " Method: void connectToExperiment" + "(Experiment exp)",
                        "The experiment to connect to is itself already connected " + "to model : " + exp.getModel(),
                        "Only one main model can be connected to an experiment at " + "a time.");
            return; // no connection possible.
        }

        if (_myExperiment != null) {
            sendWarning("Can not connect to Experiment " + exp.getName() + "! " + "Command ignored.",
                        "Model : " + getName() + " Method: void connectToExperiment" + "(Experiment exp)",
                        "The model is already connected to Experiment : " + _myExperiment.getName() + "!",
                        "A model can only be connected to just one Experiment.");
            return;
        }

        // set the experiment reference
        _myExperiment = exp;

        // register at Experiment
        _myExperiment.registerModel(this);

        // set the Experiment parameter at all submodels if necessary
        if (!_subModels.isEmpty()) {

            for (Model m : _subModels) {
                m.setConnectedExperiment(exp);
            }

        }

        // initialize the components of the main model
        init();
        // initialize the submodels recursively
        doSubmodelInit();
        // reset the model and all submodels recursively
        reset();

    }

    /**
     * Creates the default reporter associated with this model. The default reporter
     * returned by this method just prints the model's description as implemented in
     * method <code>description()</code> and appends the simulation time of the last
     * reset. If the user's model offers more information, create a special class
     * derived from class <code>desmoj.report.ModelReporter</code> to extract and
     * present that extra information.
     * 
     * @return Reportable : The reporter associated with this model
     */
    @Override
    public ModelReporter createDefaultReporter() {

        // has to be the special reporter designed for this model
        return new desmoj.core.report.ModelReporter(this);

    }

    /**
     * Should return the description of the model. Implement this method to document
     * the model's intent, creator, date and all other information that needs to be
     * published about this model in a report. Please use it generously, other
     * people interested in your model will thank you.
     * 
     * @return java.lang.String : The description of the model as a text String
     */
    public abstract String description();

    /**
     * Implement this method to schedule the entities and/or events needed for your
     * model to start in a defined setting. If a model is connected to an experiment
     * and started without any initial schedules, it will not run since no events to
     * process are on the scheduler's event-list. This method is called
     * automatically by the framework when the experiment's <code>start()</code>
     * method is called.
     */
    public abstract void doInitialSchedules();

    /**
     * Returns a list of this model's entities. The order of the list is
     * unspecified.
     * 
     * @param includeSubmodels boolean : Do include (true) or exclude (false)
     *                         submodels' entities in the list.
     * 
     * @return List<Entity> : The class will return a list of all existing objects
     *         of <code>Entity</code>.
     */
    public List<Entity> getEntities(boolean includeSubmodels) {
        List<Entity> list = new LinkedList<Entity>();
        list.addAll(_entityMap.values());

        if (includeSubmodels) {
            for (Model submodel : this._subModels) {
                list.addAll(submodel.getEntities(true));
            }
        }

        return list;
    }

    /**
     * Returns the entity to the belonging identifier.
     *
     * @return Entity : The class will return the <code>Entity</code> or null if no
     *         such exists.
     */
    public Entity getEntity(long ident) {
        return (Entity) _entityMap.get(Long.valueOf(ident));
    }

    /**
     * Returns the experiment that is connected to this model or <code>null</code>
     * if the model is not yet connected to an experiment.
     * 
     * @return Experiment : The experiment that this model is connected to or
     *         <code>null</code> if no connection is established
     */
    public Experiment getExperiment() {

        if (this.isMainModel()) {

            return _myExperiment;

        } else {

            return this.getModel().getExperiment();

        }
    }

    /**
     * Returns the Model's viewpoint of the ParameterManager
     * 
     * @return ModelParameterManager : The ParameterManager
     */
    public ModelParameterManager getParameterManager() {
        return _paramManager;
    }

    /**
     * Returns a copy of the dynamic list containing all reportable components of
     * this model.
     * 
     * @return reportables : A list containing all reportable components of this
     *         model.
     */
    public List<Reportable> getReportables() {
        return new ArrayList<Reportable>(this._reportables);
    }

    /**
     * Shows, if there are submodels registered with this model.
     * 
     * @return boolean : True, if there is at least one submodel registered at this
     *         Model, flase if there are no submodels
     */
    public boolean hasSubModels() {

        return !_subModels.isEmpty(); // checks for some submodels in vector

    }

    /**
     * Implement this method to do initialization work for your model. All
     * ModelComponents associated with this model are supposed to be instantiated in
     * this method.
     * <p>
     * <em>Caution! Do not use the Model's constructor method to instantiate your
     * modelcomponents!</em> <br>
     * This prevents the modelcomponents to get contact to the associated Experiment
     * since a Model can only be connected to an Experiment after itself has been
     * instantiated correctly. Thus it is necessary to put all initialization and
     * instantiation work into this method. Do not use this method to do the initial
     * schedules needed for the scheduler to start with. Use method
     * <code>doInitialSchedules()</code> instead. If there are no events scheduled
     * in the event-list, the scheduler will stop the simulation immediately because
     * there is nothing for him to do. If you are using submodels, take care that
     * each of the submodels has its own <code>init</code> method properly set up.
     */
    public abstract void init();

    /**
     * Initializes the model by calling method <code>init()</code> to set up all
     * model related components as specified in that method. After initializing the
     * model, the given ModelOption's <code>setOption()</code> method is called with
     * this model as parameter. This enables the user to specify different model
     * parameter settings for different experiments. It even allows automated
     * parameter variation to give a small but handy aid for model optimization.
     * Implement the ModelOptions to set the parameters for your model. Make sure to
     * react properly on the ModelOptions given. Note that specifiying ModelOptions
     * is up to the user.
     * 
     * @param options ModelOptions : The parameter settings for this model
     */
    public void init(ModelOptions options) {

        this.init(); // just call the other init after dealing with the
        // options
        options.setOptions(this);

    }

    /**
     * Shows if this model has already been connected to an experiment.
     * 
     * @return boolean : Is <code>true</code>, if model is connected to an
     *         experiment, <code>false</code> otherwise
     */
    public boolean isConnected() {

        return (_myExperiment != null); // no experiment connected

    }

    /**
     * Shows if this model is a main model and thus is not submodel of other models.
     * 
     * @return boolean : Returns <code>true</code> if the model is a main model,
     *         <code>false</code> if it is a submodel of other models
     */
    public boolean isMainModel() {

        return this.isMainModel;

    }

    /**
     * Shows if this model is submodel to another model and thus is not the main
     * models.
     * 
     * @return boolean : Returns <code>true</code> if the model is a submodel of
     *         other models, <code>false</code> if it is a main model
     */
    public boolean isSubModel() {

        return !this.isMainModel;
    }

    /**
     * Registers a reportable at this model. Reports can only be drawn, if the
     * reportbale is registered at his model.
     * 
     * @param r desmoj.core.simulator.Reportable : The reportable to be registered
     */
    public void register(Reportable r) {

        if (r == null)
            return; // do not accept null values
        else
            _reportables.add(r);

    }

    /**
     * Resets the statistic counters of this model and of all its registered
     * reportables. Also calls all submodels's <code>reset()</code> methods.
     */
    @Override
    public void reset() {

        // reset this model
        super.reset(); // reset the own obs & resetAt variables

        // reset all reportables registered here
        if (_reportables.isEmpty())
            return; // no reportables registered
        else {

            for (Reportable r : _reportables) { // loop reportables
                r.reset();
                // reset each registered
            }

        }

        // reset all registered submodels
        if (_subModels.isEmpty())
            return; // no reportables registered
        else {

            for (Model m : _subModels) { // loop submodels
                m.reset();
                // reset each registered
            }

        }

    }

    /**
     * Checks the given modelcomponent for compatibility. Needed exclusively when
     * submodels are present that want to share modelcomponents between them. The
     * standard implementation only checks if the modelcomponent <code>other</code>
     * belongs to <code>this</code> model. For use with multiple models, the user
     * has to specify which other models are compatible to this model by overloading
     * this method.
     * 
     * @return boolean : Returns <code>true</code> if the given modelcomponent is
     *         compatible to this model, <code>false</code> otherwise.
     * @param other ModelComponent : The modelcomponent that needs to be checked for
     *              compatibility to this model.
     */
    protected boolean checkCompatibility(ModelComponent other) {

        return (this == other.getModel()); // check for same owner

    }

    /**
     * Calls the <code>init()</code> method of all registered submodels of the
     * current model. Subsequently calls the <code>init()</code> method of all
     * registered submodels of every submodel.
     */
    void doSubmodelInit() {

        if (!this.hasSubModels())
            return; // no submodels registered here
        else {

            for (Model m : _subModels) { // loop submodels
                m.init(); // init submodel
                m.doSubmodelInit(); // init submodels of submodels
            }
        }
    }

    /**
     * Calls the <code>doInitialSchedules()</code> method of all registered
     * submodels of the current model. Subsequently calls the
     * <code>doSubmodelSchedules()</code> method of all registered submodels of
     * every submodel.
     */
    void doSubmodelSchedules() {

        if (!this.hasSubModels())
            return; // no submodels registered here
        else {

            for (Model m : _subModels) { // loop submodels
                m.doInitialSchedules(); // initial schedules for the submodel
                m.doSubmodelSchedules(); // initial schedules for potential submodels of the submodel
            }

        }
    }

    /**
     * Links an entity with an unique identification number.
     * 
     * @param e Entity : The new Entity to link with an unique identification
     *          number.
     * 
     * @return long : Unique identification number.
     */
    long linkWithIdentNumber(Entity e) {
        _numberOfEntitiesCreated++;
        _entityMap.put(_numberOfEntitiesCreated, e);
        return _numberOfEntitiesCreated;
    }

    /**
     * Registers another model as submodel of this model.
     * 
     * @param subModel Model : The model to be registerd as a submodel
     */
    void registerSubModel(Model subModel) {

        if (subModel == null) {
            sendWarning("Can't register Submodel!",
                        "Model : " + getName() + " Method: registerSubModel(Model " + "subModel)",
                        "The SubModel given as parameter is a null reference.",
                        "Be sure to have a valid SubModel reference before calling " + "this method.");
            return; // exit
        }

        _subModels.add(subModel);

    }

    /**
     * Produces a report about the model and all its related modelcomponents and
     * submodels. It registers the model's reporter and all other reporters at the
     * experiment's reportmanager to produce the report output. The reporters are
     * ordered into the reportmanager returned with this model's reporter first,
     * followed by all reportable's reporters. After this section, all submodel's
     * reporters are inserted according to the order they were attached to this
     * model. Each submodel's section is sorted using the criteria above. Thus all
     * reporters a re separated by model to prevent confusion.
     * 
     * @return Enumeration : The Enumeration carrying all reporters available from
     *         this model
     */
    List<Reporter> report() {

        // create the reportmanager necessary for sorting all reporters
        ReportManager repMan = new ReportManager(getName() + "_ReportManager");

        // register this model's reporter
        repMan.register(createDefaultReporter());

        // register all modelcomponent's reporters
        // in case they are set to produce a report output
        for (Reportable r : _reportables) { // loop reportables
            if (r.reportIsOn())
                repMan.register(r.getReporter());
        }

        // register all submodel's reporters
        List<Reporter> subReporters;
        // buffer for submodel-reportmanagers returned

        for (Model m : _subModels) { // loop submodels

            subReporters = m.report();

            // add all reporters from the ordered enumeration at last position
            // of repMan
            for (Reporter r : subReporters) {
                repMan.addLast(r);
            }
        }

        return repMan.elements();

    }

    /**
     * Connects submodels to the experiment given. This method is called by the main
     * model, when submodels register themselves at the main model.
     * 
     * @param e desmoj.Experiment : The experiment to connect this model to
     */
    void setConnectedExperiment(Experiment e) {

        if (e == null) {
            sendWarning("Can not connect to experiment! Command ignored.",
                        "Model : " + getName() + " Method: " + "void setConnectedExperiment (Experiment e)",
                        "The given Esperiment parameter contained a null reference",
                        "Be sure to always give valid parameters.");
            return; // do nothing
        }

        if (isSubModel()) { // this is a submodel
            _myExperiment = e;
            return;
        } else { // this is no submodel
            sendWarning("Can not connect to experiment! Command ignored.",
                        "Model : " + getName() + " Method: void " + "method setConnectedExperiment" + "(Experiment e)",
                        "This model is not a submodel.",
                        "Only submodels can be connected to an experiment using " + "this method.");
            return; // do nothing
        }

    }

    /**
     * Sets the current model to be the main model by setting the <code>owner</code>
     * reference to itself.
     */
    void setMain() {

        setOwner(this);

    }

}
