package ca.michael_cunningham.currencyconverter.tools;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Cache
 * ------------------------
 *
 * An Android compatible, custom, abstract Java class used for saving and loading files
 * in the application cache.
 *
 * @author  Michael Cunningham (http://michael-cunningham.ca)
 * @since   December 2nd, 2014
 * @version v1.0
 */
public abstract class Cache
{
    /**
     * Attempts to write a given object to the disk with the given file name
     *
     * @param file    - a generic object to be written to the disk
     * @param ctx     - context passed from a class
     * @param path    - the path to the file, including the filename
     * @return result - boolean, if the saving process was successful or not (true/false)
     */
    public static boolean saveFile(Object file, Context ctx, String path)
    {
        boolean result;

        try
        {
            // setup streams
            FileOutputStream   outFileStream   = ctx.openFileOutput(path, Context.MODE_PRIVATE);
            ObjectOutputStream outObjectStream = new ObjectOutputStream(outFileStream);

            // write values to the stream
            outObjectStream.writeObject(file);

            // output done - flush it
            outObjectStream.flush();
            outObjectStream.close();

            result = true;
        }
        catch (IOException e)
        {
            result = false;
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Attempts to read a saved file from the disk into an a generic object to return
     *
     * @param ctx      - context passed from a class
     * @param path     - the path to the file, including the filename
     * @return rtnFile - a generic object which can be casted to different object types
     */
    public static Object loadFile(Context ctx, String path)
    {
        // declare dummy converter
        Object rtnFile = null;

        try
        {
            // setup streams
            FileInputStream   inFileStream   = ctx.openFileInput(path);
            ObjectInputStream inObjectStream = new ObjectInputStream(inFileStream);

            // read values from the stream
            rtnFile = inObjectStream.readObject();

            // input done
            inObjectStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return rtnFile;
    }

    /**
     * Deletes a specified file if it exists on the disk.
     *
     * @param ctx      - context passed from a class
     * @param path     - the path to the file, including the filename
     * @return boolean - if the file was deleted or not (true/false)
     */
    public static boolean deleteFile(Context ctx, String path)
    {
        if (fileExists(ctx, path))
        {
            if (ctx.deleteFile(path))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether or not a file exists in the context's file storage.
     *
     * @param ctx      - context passed from a class
     * @param path     - the path to the file, including the filename
     * @return boolean - if the file exists or not (true/false)
     */
    public static boolean fileExists(Context ctx, String path)
    {
        File file = ctx.getFileStreamPath(path);

        return file.exists();
    }
}