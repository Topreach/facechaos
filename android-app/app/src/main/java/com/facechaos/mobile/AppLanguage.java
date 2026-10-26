package com.facechaos.mobile;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;
final class AppLanguage {
 static final String[] CODES={"","en","ar","fr","es","pt"};
 static final String[] NAMES={"", "English", "\u0627\u0644\u0639\u0631\u0628\u064a\u0629", "Fran\u00e7ais", "Espa\u00f1ol", "Portugu\u00eas"};
 static String current(Context c){if(Build.VERSION.SDK_INT>=33){LocaleList locales=c.getSystemService(android.app.LocaleManager.class).getApplicationLocales();return locales.isEmpty()?"":locales.get(0).getLanguage();}return c.getSharedPreferences("language",0).getString("code","");}
 static Context wrap(Context base){if(Build.VERSION.SDK_INT>=33)return base;String code=current(base);if(code.isEmpty())return base;Configuration config=new Configuration(base.getResources().getConfiguration());Locale locale=Locale.forLanguageTag(code);config.setLocale(locale);config.setLayoutDirection(locale);return base.createConfigurationContext(config);}
 static void set(android.app.Activity activity,String code){boolean supported=false;for(String value:CODES)if(value.equals(code))supported=true;if(!supported)throw new IllegalArgumentException("Unsupported language");if(Build.VERSION.SDK_INT>=33)activity.getSystemService(android.app.LocaleManager.class).setApplicationLocales(LocaleList.forLanguageTags(code));else{activity.getSharedPreferences("language",0).edit().putString("code",code).apply();activity.recreate();}}
}
