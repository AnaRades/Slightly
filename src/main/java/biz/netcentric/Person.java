package biz.netcentric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java Bean Person
 */
public class Person {

	private static final String[] childrenNames = new String[] {"Anna", "Berta", "Clara", "Joseph"};
	
	static {
		
	}
	
	
	private static final List<Person> friends = new ArrayList<Person>() {
        private static final long serialVersionUID = 8751914654566394666L;
        {
            add(new Person("Maria", "Fernando", false, 1));
            add(new Person("Veronica", "Jose", true, 2));
            add(new Person("Anna", "Dante", true, 1));
        }
    };

    private static final List<Person> persons = new ArrayList<Person>() {
        private static final long serialVersionUID = 8751914654566394666L;
        {
            add(new Person("Kerstin", "Jose", false, 1));
            add(new Person("Erik", "Dora", true, 3));
            add(new Person("Svajune", "Thomas", true, 0));
        }
    };

    private String name;
    private boolean married;
    private Person spouse;
    private List<String> children;

    public Person() {
        this.name = "";
        
        this.married = false;
        this.spouse = null;
        children = Collections.emptyList();
    }

    private Person(String name, Person spouse) {
    	this.name = name;
    	this.married = spouse.married;
    	this.spouse = spouse;
    	this.children = spouse.children;
    }
    
    public Person(final String name, final String spouse, final boolean isMarried, final int numberOfChildren) {
        super();
        this.name = name;
        this.spouse = new Person(spouse, this);
        this.married = isMarried;
        children = new ArrayList<String>();

        for (int i = 0; i < numberOfChildren; i++) {
            children.add("Child " + childrenNames[i]);
        }
    }

    public static Person lookup(final String id) {
        if (id != null) {
            final int personId = Integer.valueOf(id);
            if (personId > 0 && personId <= persons.size()) {
                return persons.get(personId - 1);
            }
        }

        return new Person("Empty Name", "Empty spouse", false, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Keeping at String to preserve initial API call
     * @return Spouse's name
     */
    public String getSpouse() {
        return this.spouse.name;
    }
    
    public Person getSpouseObj() {
    	return this.spouse;
    }

    public void setSpouse(final String spouse) {
        this.spouse = new Person(spouse, this);
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(final boolean married) {
        this.married = married;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(final List<String> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", married=" + married + ", spouse="
                + getSpouse() + ", children=" + children + "]";
    }
}
