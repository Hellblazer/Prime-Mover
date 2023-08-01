package desmoj.core.simulator;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * The progress bar to display the progress of the experiment.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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
 * @sharpen.ignore
 * 
 */
public class ExpProgressBar extends JFrame {

	/**
	 * A constant defining half a second as 500 milliseconds. Every half a
	 * second the progress bar will be updated.
	 */
	public final static int HALF_A_SECOND = 500;

	private static final long serialVersionUID = 1L;

	/**
     * Flag indicating whether the progress bar will be autoclosed
     * once the experiment terminates or not.
     */
    private boolean _autoclose;

	/**
	 * The experiment this ExpProgressBar is connected to.
	 */
	private Experiment _myExperiment;

    /**
	 * The ProgressBar displaying the progress of the experiment.
	 */
	private JProgressBar _progressBar;
	
	/**
	 * The timer timing the update of the progress bar.
	 */
	private Timer _timer;

	/**
	 * Constructs an ExpProgressBar for an <code>Experiment</code> to display
	 * its progress on the screen.
	 * 
	 * @param experiment
	 *            Experiment : The experiment which progress will be displayed
	 *            of the progress bar.
	 */
	public ExpProgressBar(Experiment experiment, boolean autoclose) {

		super("Progress of the experiment"); // make a JFrame

		this._myExperiment = experiment;
		this._autoclose = autoclose;

		setTitle("Progress of " + _myExperiment.getName());

		// create the UI
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setValue(0);
		_progressBar.setStringPainted(true);
		// set the preferred size
		_progressBar.setPreferredSize(new java.awt.Dimension(320, 22));

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(_progressBar, BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(contentPane);

		// Create a timer.
		_timer = new Timer(HALF_A_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				long crntTime = _myExperiment.getSimClock().getTime().getTimeInEpsilon();
		        long start = TimeOperations.getStartTime().getTimeInEpsilon();
				long stop = _myExperiment.getStopTime().getTimeInEpsilon();
		        int progress = (int) (100.0D * (crntTime - start) / (stop - start));
				_progressBar.setValue(progress);
				if (_myExperiment.isAborted()) {
					Toolkit.getDefaultToolkit().beep();
					_timer.stop();
					if (ExpProgressBar.this._autoclose) {
					   ExpProgressBar.this.setVisible(false);
					   ExpProgressBar.this.dispose();
					}
				}
			}
		});

		// start the timer
		_timer.start();
	}

	/**
	 * Returns the <code>Experiment</code> this progress bar monitors.
	 * 
	 * @return desmoj.Experiment : the <code>Experiment</code> this progress
	 *         bar monitors.
	 */
	public Experiment getExperiment() {

		return _myExperiment;
	}
}