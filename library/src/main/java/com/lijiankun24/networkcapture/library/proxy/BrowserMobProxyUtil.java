package com.lijiankun24.networkcapture.library.proxy;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.lijiankun24.networkcapture.library.har.Har;
import com.lijiankun24.networkcapture.library.har.HarEntry;
import com.lijiankun24.networkcapture.library.har.HarLog;
import com.lijiankun24.networkcapture.library.har.HarPage;

import java.util.HashSet;
import java.util.Set;

public class BrowserMobProxyUtil {

    private static final Supplier<String> version = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            return "Version 1.0.0";
        }
    });

    public static String getVersionString() {
        return version.get();
    }

    public static Har copyHarThroughPageRef(Har har, String pageRef) {
        if (har == null) {
            return null;
        }

        if (har.getLog() == null) {
            return new Har();
        }

        // collect the page refs that need to be copied to new har copy.
        Set<String> pageRefsToCopy = new HashSet<String>();

        for (HarPage page : har.getLog().getPages()) {
            pageRefsToCopy.add(page.getId());

            if (pageRef.equals(page.getId())) {
                break;
            }
        }

        HarLog logCopy = new HarLog();

        // copy every entry and page in the HarLog that matches a pageRefToCopy. since getEntries() and getPages() return
        // lists, we are guaranteed that we will iterate through the pages and entries in the proper order
        for (HarEntry entry : har.getLog().getEntries()) {
            if (pageRefsToCopy.contains(entry.getPageref())) {
                logCopy.addEntry(entry);
            }
        }

        for (HarPage page : har.getLog().getPages()) {
            if (pageRefsToCopy.contains(page.getId())) {
                logCopy.addPage(page);
            }
        }

        Har harCopy = new Har();
        harCopy.setLog(logCopy);

        return harCopy;
    }
}
