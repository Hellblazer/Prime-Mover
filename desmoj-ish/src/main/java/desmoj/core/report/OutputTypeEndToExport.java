package desmoj.core.report;

/**
 * Interface for Outputs that cannot be written line by line, e.g. Excel.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @date 30.03.2011
 * @author Xiufeng Li
 */

public interface OutputTypeEndToExport extends OutputType
{
	/**
	 * Export a new few file for the writting output.
	 * 
	 * @param pathname
	 *            String: path to write in
	 * @param filename
	 *            String: name of the file
	 */
	public void export(String pathname, String filename);
}
