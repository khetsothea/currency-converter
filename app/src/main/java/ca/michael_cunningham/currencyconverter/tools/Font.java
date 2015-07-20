package ca.michael_cunningham.currencyconverter.tools;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Font
 * ------------------------
 *
 * An Android compatible, custom, abstract Java class used for getting a typeface file
 * in the assets folder.
 *
 * @author  Michael Cunningham (http://michael-cunningham.ca)
 * @since   December 2nd, 2014
 * @version v1.0
 */
public abstract class Font
{
    /**
     * Gets a Typeface of the specified path in the assets folder
     *
     * @param ctx  - the context from an activity class
     * @param path - the path to the font file
     * @return     - a final typeface object to apply to a view
     */
    public static Typeface getTypeFace(Context ctx, String path)
    {
        return Typeface.createFromAsset(ctx.getAssets(), path);
    }
}