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

    public ElementBuilder node(String name) {
        return node(name, eb -> {
        });
    }

    public ElementBuilder node(String name, Consumer<ElementBuilder> cb) {
        Element child = doc.createElement(name);
        if(root == null) {
            doc.appendChild(child);
        } else {
            root.appendChild(child);
        }
        ElementBuilder eb = new ElementBuilder(doc, child);
        cb.accept(eb);
        return eb;
    }

    public final ElementBuilder node(String name, String textContent, Map<String, String> attrs) {
        return node(name, eb -> {
            if(textContent != null) eb.text(textContent);
            for(Map.Entry<String, String> attr : attrs.entrySet()) {
                eb.attr(attr.getKey(), attr.getValue());
            }
        });
    }

    @SafeVarargs
    public final ElementBuilder node(String name, String textContent, Tuple2<String, String>...attrs) {
        MapBuilder<String, String> mapBuilder = new MapBuilder<>();
        for(Tuple2<String, String> attr : attrs) {
            mapBuilder.entry(attr.get_1(), attr.get_2());
        }
        return node(name, textContent, mapBuilder.build(TreeMap::new));
    }

    @SafeVarargs
    public final ElementBuilder node(String name, Tuple2<String, String>...attrs) {
        return node(name, null, attrs);
    }

    public final ElementBuilder node(String name, Map<String, String> attrs) {
        return node(name, null, attrs);
    }

    public ElementBuilder text(String textContent) {
        root.setTextContent(textContent);
        return this;
    }

    public ElementBuilder attr(String name, String value) {
        root.setAttribute(name, value);
        return this;
    }


}
