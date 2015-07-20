package ca.michael_cunningham.currencyconverter.interfaces;

/**
 * OnTaskStarted
 * ------------------------
 *
 * Interface which designates if a certain task has been started
 *
 * @author  Michael Cunningham (http://michael-cunningham.ca)
 * @since   December 1st, 2014
 * @version v1.0
 */
public interface OnTaskStarted
{
    /**
     * Designates if a task has been started
     *
     * @param type - the type of task which has been started
     */
    void onTaskStarted(byte type);
}