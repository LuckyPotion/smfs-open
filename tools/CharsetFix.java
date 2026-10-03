import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Repair source files whose Chinese text was mangled by a compiler that read
 * UTF-8 bytes using the GBK code page.
 *
 * The damage is reversible:  damaged = GBK.decode(utf8Bytes)
 * so the repair is:          original = GB18030.decode(GBK.encode(damaged))
 *
 * GB18030 (not UTF-8) is required for the final decode because some characters that
 * survive a UTF-8 round trip - e.g. 蟹, which is U+87F9 in Unicode but has no GBK
 * code point and therefore falls outside the basic BMP subset the mangling covers -
 * are only recoverable through the GB18030 mapping.
 *
 * Usage: CharsetFix <file-or-dir> [more...]        (files are rewritten in place)
 *        CharsetFix --check <file-or-dir> [...]    (report only, write nothing)
 */
public final class CharsetFix {
    private static final Charset GBK = Charset.forName("GBK");
    private static final Charset GB18030 = Charset.forName("GB18030");

    private static final char REPLACEMENT = '\uFFFD';

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        boolean checkOnly = false;
        List<String> targets = new ArrayList<>();
        for (String a : args) {
            if ("--check".equals(a)) checkOnly = true;
            else targets.add(a);
        }

        if (targets.isEmpty()) {
            out.println("usage: CharsetFix [--check] <file-or-dir> [more...]");
            System.exit(2);
        }

        int files = 0, chars = 0, skipped = 0;
        for (String t : targets) {
            Path p = Paths.get(t);
            if (Files.isDirectory(p)) {
                List<Path> list;
                try (Stream<Path> s = Files.walk(p)) {
                    list = s.filter(Files::isRegularFile)
                             .filter(x -> {
                                 String n = x.getFileName().toString();
                                 return n.endsWith(".java") || n.endsWith(".json") || n.endsWith(".mcmeta");
                             })
                             .toList();
                }
                for (Path f : list) {
                    int n = fix(f, checkOnly, out);
                    if (n > 0) { files++; chars += n; } else if (n < 0) skipped++;
                }
            } else if (Files.isRegularFile(p)) {
                int n = fix(p, checkOnly, out);
                if (n > 0) { files++; chars += n; } else if (n < 0) skipped++;
            } else {
                out.println("skip (missing): " + p);
            }
        }
        out.println("[fix] " + (checkOnly ? "would rewrite " : "rewrote ") + files
                + " file(s), recovered " + chars + " CJK char(s), left " + skipped + " unchanged");
    }

    /** @return recovered CJK count (&gt;0 = changed), 0 = nothing to do, -1 = unsafe, skipped */
    private static int fix(Path file, boolean checkOnly, PrintStream out) throws IOException {
        byte[] raw = Files.readAllBytes(file);
        String text = new String(raw, StandardCharsets.UTF_8);

        int before = countCjk(text);
        if (before == 0) return 0;
        if (!hasMojibakeSignature(text)) return 0;

        byte[] gbk;
        try {
            gbk = text.getBytes(GBK);
        } catch (Exception e) {
            return -1;
        }

        String viaUtf8 = new String(gbk, StandardCharsets.UTF_8);
        String viaGb18030 = new String(gbk, GB18030);

        // A genuine mojibake repair always changes the text. When the round trip is a
        // no-op the file already holds correct text, so leave it completely alone.
        if (viaGb18030.equals(text) && viaUtf8.equals(text)) return 0;

        String best;
        if (isClean(viaGb18030, text)) {
            best = viaGb18030;
        } else if (isClean(viaUtf8, text)) {
            best = viaUtf8;
        } else {
            return -1;
        }
        if (best.equals(text)) return 0;

        int after = countCjk(best);
        if (!checkOnly) {
            Files.write(file, best.getBytes(StandardCharsets.UTF_8));
        }
        out.println("  " + (checkOnly ? "would fix" : "fixed") + ": " + file
                + "  (" + before + " -> " + after + " CJK)");
        return after;
    }

    /** Mojibake text is dominated by CJK characters, so require a high CJK ratio. */
    private static boolean hasMojibakeSignature(String text) {
        int cjk = 0, letters = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) cjk++;
            else if (Character.isLetterOrDigit(c) || c == '_') letters++;
        }
        int considered = cjk + letters;
        if (considered == 0) return false;
        return cjk * 100 / considered >= 20;
    }

    private static boolean isClean(String s, String original) {
        if (s.indexOf(REPLACEMENT) >= 0) return false;
        return countCjk(s) > 0;
    }

    private static int countCjk(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) n++;
        }
        return n;
    }
}
