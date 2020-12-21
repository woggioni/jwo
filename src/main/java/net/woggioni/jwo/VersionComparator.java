package net.woggioni.jwo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class VersionComparator implements Comparator<String> {

    private static int indexOf(char[] haystack, char needle, int start) {
        int result = -1;
        for (int i = start; i < haystack.length; ++i) {
            if (haystack[i] == needle) {
                result = i;
                break;
            }
        }
        return result;
    }

    private static int indexOf(char[] haystack, char needle) {
        return indexOf(haystack, needle, 0);
    }

    private static char[] cstring(String s) {
        char[] result = new char[s.length() + 1];
        for(int i=0; i<s.length(); i++) {
            result[i] = s.charAt(i);
        }
        result[s.length()] = '\0';
        return result;
    }

    private static boolean cstringEquals(char[] cstring1, int begin1, char[] cstring2, int begin2) {
        int i=0;
        while(i < cstring1.length + begin1 || i < cstring2.length + begin2) {
            char c1 = cstring1[begin1 + i];
            char c2 = cstring2[begin2 + i];
            if(c1 != c2) return false;
            else if(c1 == '\0') break;
            i++;
        }
        return true;
    }

    private static int strlen(char[] cstring, int begin) {
        int i = begin;
        while(i < cstring.length) {
            if(cstring[i] == '\0') break;
            ++i;
        }
        return i - begin;
    }

    private static int strlen(char[] cstring) {
        return strlen(cstring, 0);
    }

    private static int strcmp(char[] cstring1, int begin1, char[] cstring2, int begin2) {
        int i = 0;
        int lim = Math.min(strlen(cstring1, begin1), strlen(cstring2, begin2));
        while(i < lim) {
            char c1 = cstring1[begin1 + i];
            char c2 = cstring2[begin2 + i];
            if(c1 < c2) {
                return -1;
            } else if(c1 > c2) {
                return 1;
            }
            ++i;
        }
        return Integer.compare(cstring1.length, cstring2.length);
    }

    /**
     * Split EVR into epoch, version, and release components.
     * @param text		[epoch:]version[-release] string
     * @param evr		array that will contain starting indexes of epoch, version, and release
     */
    private static void parseEVR(char[] text, int[] evr) {
        int epoch;
        int version;
        int release;

        int s = 0;
        /* s points to epoch terminator */
        while (s < text.length && Character.isDigit(text[s])) s++;
        /* se points to version terminator */
        int se = indexOf(text, '-', s);

        if(text[s] == ':') {
            epoch = 0;
            text[s++] = '\0';
            version = s;
            if(text[epoch] == '\0') {
                epoch = -1;
            }
        } else {
            /* different from RPM- always assume 0 epoch */
            epoch = -1;
            version = 0;
        }
        if(se != -1) {
            text[se++] = '\0';
            release = se;
        } else {
            release = -1;
        }
        evr[0] = epoch;
        evr[1] = version;
        evr[2] = release;
    }


    private static int rpmvercmp(char[] chars1, int start1, char[] chars2, int start2) {
        // easy comparison to see if versions are identical
        if(strcmp(chars1, start1, chars2, start2) == 0) return 0;
        char[] str1 = Arrays.copyOfRange(chars1, start1, start1 + strlen(chars1, start1) + 1);
        char[] str2 = Arrays.copyOfRange(chars2, start2, start2 + strlen(chars2, start2) + 1);
        int one = 0, two = 0;
        int ptr1 = 0, ptr2 = 0;
        boolean isNum;
        char oldch1, oldch2;

        // loop through each version segment of str1 and str2 and compare them
        while(str1[one] != '\0' && str2[two] != '\0') {
            char c1;
            while(true) {
                c1 = str1[one];
                if(c1 == '\0' || Character.isAlphabetic(c1) || Character.isDigit(c1)) break;
                one++;
            }

            char c2;
            while(true) {
                c2 = str2[two];
                if(c2 == '\0' || Character.isAlphabetic(c2) || Character.isDigit(c2)) break;
                two++;
            }

            // If we ran to the end of either, we are finished with the loop
            if (c1  == '\0' || c2 == '\0') break;

            // If the separator lengths were different, we are also finished
            if ((one - ptr1) != (two - ptr2)) {
                return (one - ptr1) < (two - ptr2) ? -1 : 1;
            }

            ptr1 = one;
            ptr2 = two;

            // grab first completely alpha or completely numeric segment
            // leave one and two pointing to the start of the alpha or numeric
            // segment and walk ptr1 and ptr2 to end of segment
            if (Character.isDigit(str1[ptr1])) {
                while(true) {
                    c1 = str1[ptr1];
                    if(c1 == '\0' || !Character.isDigit(c1)) break;
                    ptr1++;
                }
                while(true) {
                    c2 = str2[ptr2];
                    if(c2 == '\0' || !Character.isDigit(c2)) break;
                    ptr2++;
                }
                isNum = true;
            } else {
                while(true) {
                    c1 = str1[ptr1];
                    if(c1 == '\0' || !Character.isAlphabetic(c1)) break;
                    ptr1++;
                }
                while(true) {
                    c2 = str2[ptr2];
                    if(c2 == '\0' || !Character.isAlphabetic(c2)) break;
                    ptr2++;
                }
                isNum = false;
            }

            // save character at the end of the alpha or numeric segment
            // so that they can be restored after the comparison
            oldch1 = str1[ptr1];
            str1[ptr1] = '\0';
            oldch2 = str2[ptr2];
            str2[ptr2] = '\0';

            // this cannot happen, as we previously tested to make sure that
            // the first string has a non-null segment
            if (one == ptr1) {
                return -1;	// arbitrary
            }

            // take care of the case where the two version segments are
            // different types: one numeric, the other alpha (i.e. empty)
            // numeric segments are always newer than alpha segments
            // XXX See patch #60884 (and details) from bugzilla #50977.
            if (two == ptr2) {
                return isNum ? 1 : -1;
            }
            if (isNum) {
                /* this used to be done by converting the digit segments */
                /* to ints using atoi() - it's changed because long  */
                /* digit segments can overflow an int - this should fix that. */

                /* throw away any leading zeros - it's a number, right? */
                while (str1[one] == '0') one++;
                while (str2[two] == '0') two++;

                /* whichever number has more digits wins */
                int len1 = strlen(str1, one);
                int len2 = strlen(str2, two);
                if (len1 > len2) {
                    return 1;
                } else if (len2 > len1) {
                    return -1;
                }
            }
            // strcmp will return which one is greater - even if the two
            // segments are alpha or if they are numeric.  don't return
            // if they are equal because there might be more segments to
            // compare
            int rc = strcmp(str1, one, str2, two);
            if (rc != 0) return rc;

            // restore character that was replaced by null above
            str1[ptr1] = oldch1;
            one = ptr1;
            str2[ptr2] = oldch2;
            two = ptr2;
        }

        // this catches the case where all numeric and alpha segments have
        // compared identically but the segment separating characters were
        // different
        if (str1[one] == '\0' && str2[two] == '\0') {
            return 0;
        }

        /* the final showdown. we never want a remaining alpha string to
         * beat an empty string. the logic is a bit weird, but:
         * - if one is empty and two is not an alpha, two is newer.
         * - if one is an alpha, two is newer.
         * - otherwise one is newer.
         */
        if ((str1[one] == '\0' && !Character.isAlphabetic(str2[two]))
			|| Character.isAlphabetic(str1[one])) {
            return -1;
        } else {
            return 1;
        }
    }

    private static char[] defaultEpoch = new char[] {'0'};

    public static int cmp(String v1, String v2) {
        if(v1 == null && v2 == null) return 0;
        else if(v1 == null) return -1;
        else if(v2 == null) return 1;
        else if(Objects.equals(v1, v2)) return 0;

        char[] chars1 = cstring(v1);
        char[] chars2 = cstring(v2);
        int[] evr = new int[3];
        parseEVR(chars1, evr);
        int epoch1 = evr[0];
        int version1 = evr[1];
        int release1 = evr[2];
        parseEVR(chars2, evr);
        int epoch2 = evr[0];
        int version2 = evr[1];
        int release2 = evr[2];

        char[] seq1 = epoch1 == -1 ? defaultEpoch : chars1;
        char[] seq2 = epoch2 == -1 ? defaultEpoch : chars2;
        int ret = rpmvercmp(seq1, epoch1 == -1 ? 0 : epoch1, seq2, epoch2 == -1 ? 0 : epoch2);
        if(ret == 0) {
            ret = rpmvercmp(chars1, version1, chars2, version2);
            if(ret == 0 && release1 != -1 && release2 != -1) {
                ret = rpmvercmp(chars1, release1, chars2, release2);
            }
        }
        return ret;
    }

    @Override
    public int compare(String v1, String v2) {
        return cmp(v1, v2);
    }
}
