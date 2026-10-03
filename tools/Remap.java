import net.fabricmc.tinyremapper.InputTag;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Remap a Fabric mod jar between mapping namespaces using TinyRemapper, with the Mixin
 * extension attached so that @Mixin(...) targets and @Inject/@Accessor payloads are
 * rewritten alongside the bytecode.
 *
 * Usage:
 *   Remap <in.jar> <out.jar> <mappings.tiny> <fromNs> <toNs> [--reverse] [-cp <path>]...
 */
public final class Remap {
    public static void main(String[] args) throws Exception {
        List<String> positional = new ArrayList<>();
        List<Path> classpath = new ArrayList<>();
        boolean reverse = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--reverse" -> reverse = true;
                case "-cp", "--classpath" -> {
                    if (++i >= args.length) throw new IllegalArgumentException("-cp requires a value");
                    classpath.add(Paths.get(args[i]));
                }
                default -> positional.add(args[i]);
            }
        }

        if (positional.size() < 5) {
            System.err.println("usage: Remap <in.jar> <out.jar> <mappings.tiny> <fromNs> <toNs> [--reverse] [-cp <path>]...");
            System.exit(2);
        }

        Path in = Paths.get(positional.get(0));
        Path out = Paths.get(positional.get(1));
        Path mappings = Paths.get(positional.get(2));
        String fromNs = positional.get(3);
        String toNs = positional.get(4);

        if (!Files.isRegularFile(in)) throw new IllegalArgumentException("input not found: " + in);
        if (!Files.isRegularFile(mappings)) throw new IllegalArgumentException("mappings not found: " + mappings);
        if (!Files.isRegularFile(out)) Files.createDirectories(out.toAbsolutePath().getParent());
        if (Files.exists(out)) Files.delete(out);

        System.out.println("[remap] input      : " + in);
        System.out.println("[remap] output     : " + out);
        System.out.println("[remap] mappings   : " + mappings);
        System.out.println("[remap] namespaces : " + fromNs + " -> " + toNs + (reverse ? " (reverse)" : ""));
        System.out.println("[remap] classpath  : " + classpath.size() + " entries");

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        TinyRemapper.Builder builder = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings, fromNs, toNs))
                .threads(threads)
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .invalidLvNamePattern(Pattern.compile("\\$\\$\\d+"))
                .inferNameFromSameLvIndex(true);

        // Attach the mixin extension to *this* builder so annotation payloads remap too.
        new MixinExtension(Collections.emptySet()).attach(builder);

        TinyRemapper remapper = builder.build();

        try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(out).build()) {
            // Non-class resources (fabric.mod.json, assets/, data/, mixins json) are carried
            // over verbatim; the single-arg overload performs no resource remapping.
            consumer.addNonClassFiles(in);
            if (!classpath.isEmpty()) {
                remapper.readClassPath(classpath.toArray(new Path[0]));
            }
            InputTag tag = remapper.createInputTag();
            remapper.readInputs(tag, in);
            remapper.apply(consumer, tag);
        } finally {
            remapper.finish();
        }

        System.out.println("[remap] done, size = " + Files.size(out) + " bytes");
    }
}
