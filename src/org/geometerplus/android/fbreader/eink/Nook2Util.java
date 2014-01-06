package org.geometerplus.android.fbreader.eink;

/**
 * Nook Touch EPD controller interface wrapper.
 * @author DairyKnight <dairyknight@gmail.com>
 * http://forum.xda-developers.com/showthread.php?t=1183173
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Activity;

public class Nook2Util {

    private static Class epdControllerClass;

    private  static Class epdControllerRegionClass;
    private  static Class epdControllerRegionParamsClass;
    private  static Class epdControllerWaveClass;
    private  static Class epdControllerModeClass;

    private static Object[] waveEnums;

    private static Object[] regionEnums;

    private static Object[] modeEnums;

    private static boolean successful = false;
    static {
        try {
            /*
			 * Loading the Epson EPD Controller Classes
			 *
			 * */
        	ZLAndroidLibrary lib = (ZLAndroidLibrary) ZLibrary.Instance();
            epdControllerClass = Class
                        .forName("android.hardware.EpdController");
            epdControllerRegionClass = Class
                    .forName("android.hardware.EpdController$Region");
            if (lib.getDevice().equals(ZLAndroidLibrary.Device.NOOK12)) {
            	epdControllerRegionParamsClass = Class
            			.forName("android.hardware.EpdRegionParams");
            	epdControllerWaveClass = Class
    					.forName("android.hardware.EpdRegionParams$Wave");
            } else {
            	epdControllerRegionParamsClass = Class
            			.forName("android.hardware.EpdController$RegionParams");
            	 epdControllerWaveClass = Class
                         .forName("android.hardware.EpdController$Wave");
            }
            epdControllerModeClass = Class
                    .forName("android.hardware.EpdController$Mode");

            if (epdControllerWaveClass.isEnum()) {
				System.err.println("EpdController Wave Enum successfully retrived.");
				waveEnums = epdControllerWaveClass.getEnumConstants();
			}

			if (epdControllerRegionClass.isEnum()) {
				System.err.println("EpdController Region Enum successfully retrived.");
				regionEnums = epdControllerRegionClass.getEnumConstants();
			}


            if (epdControllerModeClass.isEnum()) {
				System.err.println("EpdController Region Enum successfully retrived.");
				modeEnums = epdControllerModeClass.getEnumConstants();
				System.err.println(modeEnums);
			}

            successful = true;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


	private static Object mEpdController = null;

	static void setGL16Mode(Activity a) {
		System.err.println("setGL16Mode");
		ZLAndroidLibrary lib = (ZLAndroidLibrary) ZLibrary.Instance();
		try {
            if (successful) {
            	if (lib.getDevice().equals(ZLAndroidLibrary.Device.NOOK12) && mEpdController == null) {
            		Constructor[] EpdControllerConstructors = epdControllerClass.getConstructors();
					mEpdController = EpdControllerConstructors[0].newInstance(new Object[] { a });
				}
                Constructor RegionParamsConstructor = epdControllerRegionParamsClass
                        .getConstructor(new Class[] { Integer.TYPE, Integer.TYPE,
                                Integer.TYPE, Integer.TYPE, epdControllerWaveClass});

                Object localRegionParams = RegionParamsConstructor
                        .newInstance(new Object[] { 0, 0, 600, 800, waveEnums[1]}); // Wave = GU

                Method epdControllerSetRegionMethod = epdControllerClass.getMethod(
                        "setRegion", new Class[] { String.class,
                                epdControllerRegionClass,
                                epdControllerRegionParamsClass, epdControllerModeClass });
                if (lib.getDevice().equals(ZLAndroidLibrary.Device.NOOK12)) {
                	epdControllerSetRegionMethod
					.invoke(mEpdController, new Object[] { "FBReaderJ",
							regionEnums[2], localRegionParams, modeEnums[2] }); // Mode = ONESHOT_ALL
                } else {
	                epdControllerSetRegionMethod
	                        .invoke(null, new Object[] { "FBReaderJ",
	                                regionEnums[2], localRegionParams, modeEnums[2] }); // Mode = ONESHOT_ALL
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
