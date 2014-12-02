package com.tinkerpop.gremlin.tinkergraph.structure;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.util.ElementHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TinkerElement implements Element, Element.Iterators {

    protected Map<String, List<Property>> properties = new HashMap<>();
    protected final Object id;
    protected final String label;
    protected final TinkerGraph graph;
    protected boolean removed = false;

    protected TinkerElement(final Object id, final String label, final TinkerGraph graph) {
        this.graph = graph;
        this.id = id;
        this.label = label;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public Set<String> keys() {
        return TinkerHelper.inComputerMode(this.graph) ?
                Element.super.keys() :
                this.properties.keySet().stream().filter(key -> !Graph.Key.isHidden(key)).collect(Collectors.toSet());
    }

    @Override
    public Set<String> hiddenKeys() {
        return TinkerHelper.inComputerMode(this.graph) ?
                Element.super.hiddenKeys() :
                this.properties.keySet().stream().filter(Graph.Key::isHidden).collect(Collectors.toSet());
    }

    @Override
    public <V> Property<V> property(final String key) {
        if (this.removed) throw Element.Exceptions.elementAlreadyRemoved(this.getClass(), this.id);
        if (TinkerHelper.inComputerMode(this.graph)) {
            final List<Property> list = this.graph.graphView.getProperty(this, key);
            return list.size() == 0 ? Property.<V>empty() : list.get(0);
        } else {
            return this.properties.containsKey(key) ? this.properties.get(key).get(0) : Property.<V>empty();
        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    //////////////////////////////////////////////

    @Override
    public <V> Iterator<? extends Property<V>> propertyIterator(final String... propertyKeys) {
        return (Iterator) (TinkerHelper.inComputerMode(this.graph) ?
                this.graph.graphView.getProperties(TinkerElement.this).stream().filter(p -> ElementHelper.keyExists(p.key(), propertyKeys)).iterator() :
                this.properties.entrySet().stream().filter(entry -> ElementHelper.keyExists(entry.getKey(),propertyKeys)).flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList()).iterator());
    }


}
