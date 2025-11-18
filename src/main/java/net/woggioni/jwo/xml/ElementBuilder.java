package net.woggioni.jwo.xml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.woggioni.jwo.MapBuilder;
import net.woggioni.jwo.Tuple2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ElementBuilder {

    private final Document doc;

    @Getter
    private final Element root;

    public ElementBuilder node(final String name) {
        return node(name, eb -> {
        });
    }

    public ElementBuilder node(final String name, final Consumer<ElementBuilder> cb) {
        final Element child = doc.createElement(name);
        if(root == null) {
            doc.appendChild(child);
        } else {
            root.appendChild(child);
        }
        final ElementBuilder eb = new ElementBuilder(doc, child);
        cb.accept(eb);
        return eb;
    }

    public final ElementBuilder node(final String name, final String textContent, final Map<String, String> attrs) {
        return node(name, eb -> {
            if(textContent != null) eb.text(textContent);
            for(final Map.Entry<String, String> attr : attrs.entrySet()) {
                eb.attr(attr.getKey(), attr.getValue());
            }
        });
    }

    @SafeVarargs
    public final ElementBuilder node(final String name, final String textContent, final Tuple2<String, String>...attrs) {
        final MapBuilder<String, String> mapBuilder = new MapBuilder<>();
        for(final Tuple2<String, String> attr : attrs) {
            mapBuilder.entry(attr.get_1(), attr.get_2());
        }
        return node(name, textContent, mapBuilder.build(TreeMap::new));
    }

    @SafeVarargs
    public final ElementBuilder node(final String name, final Tuple2<String, String>...attrs) {
        return node(name, null, attrs);
    }

    public final ElementBuilder node(final String name, final Map<String, String> attrs) {
        return node(name, null, attrs);
    }

    public ElementBuilder text(final String textContent) {
        root.setTextContent(textContent);
        return this;
    }

    public ElementBuilder attr(final String name, final String value) {
        root.setAttribute(name, value);
        return this;
    }
}
