package ca.michael_cunningham.currencyconverter.interfaces;

/**
 * OnTaskCompleted
 * ------------------------
 *
 * Interface which designates if a certain task has been completed
 *
 * @author  Michael Cunningham (www.michael-cunningham.ca)
 * @since   December 1st, 2014
 * @version v1.0
 */
public interface OnTaskCompleted {

    /**
     * Designates if a task has been completed
     *
     * @param type - the type of task which has been started
     */
    void onTaskCompleted(byte type);

}