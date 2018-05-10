package biz.netcentric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java Bean Person
 */
public class Person {

	private static final String[] childrenNames = new String[] {"Anna", "Berta", "Clara", "Joseph"};


    private static final List<Person> friends = new ArrayList<Person>() {
        private static final long serialVersionUID = 8751914654566394666L;
        {
            add(new Person("Justin", "Dana", false, 1));
            add(new Person("Paolo", "Janice", true, 3));
            add(new Person("Bernadette", "Antonio", true, 0));
            add(new Person("Bobby", "Susan", false, 1));
            add(new Person("Curly", "Maria", true, 3));
            add(new Person("Moe", "Marimar", true, 0));
        }
    };
    
    private static final List<Person> persons = new ArrayList<Person>() {
        private static final long serialVersionUID = 8751914654566394666L;
        {
        	//weird way to add best friends, data-for-x testing
            add((new Person("Kerstin", "Jose", false, 1)).addBestFriend(friends.get(0)).addBestFriend(friends.get(1)).addBestFriend(friends.get(2)));
            add((new Person("Erik", "Dora", true, 3)).addBestFriend(friends.get(2)).addBestFriend(friends.get(3)).addBestFriend(friends.get(4)));
            add((new Person("Svajune", "Thomas", true, 0)).addBestFriend(friends.get(4)).addBestFriend(friends.get(5)).addBestFriend(friends.get(1)));
        }
    };
    
    private String name;
    private boolean married;
    private Person spouse;
    private List<String> children;
    private List<Person> bestFriends;
    private boolean hasChildren;

    public Person() {
        this.name = "";
        
        this.married = false;
        this.spouse = null;
        this.hasChildren = false;
        this.children = Collections.emptyList();
        this.bestFriends = new ArrayList<>();        
    }

    private Person(String name, Person spouse) {
    	this.name = name;
    	this.married = spouse.married;
    	this.spouse = spouse;
    	this.children = spouse.children;
    	this.hasChildren = spouse.hasChildren;
    	this.bestFriends = spouse.bestFriends;
    }
    
    public Person(final String name, final String spouse, final boolean isMarried, final int numberOfChildren) {
        this();
        this.name = name;
        this.spouse = new Person(spouse, this);
        this.married = isMarried;
        this.hasChildren = (numberOfChildren > 0);
        
        this.children = new ArrayList<String>();
        for (int i = 0; i < numberOfChildren; i++) {
            children.add(childrenNames[i]);
        }        
    }

    private Person addBestFriend(Person friend) {
    	bestFriends.add(friend);
    	return this;
    }
    /**
     * 
     * @param id index of person
     * @return Person at index, or default person in case of invalid index
     */
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
     * Keeping as String to preserve initial API call
     * @return Spouse's name
     */
    public String getSpouse() {
        return this.spouse.name;
    }
    
    /**
     * Introduced in order to test local variables
     * @return This person's Spouse as Person object
     */
    public Person getSpouseObj() {
    	return this.spouse;
    }

    /**
     * Creates a new person as spouse
     * @param spouse Spouse name
     */
    public void setSpouse(final String spouse) {
        this.spouse = new Person(spouse, this);
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(final boolean married) {
        this.married = married;
    }
    
    /**
     * Used this naming to ensure mapping between Java and javascript 
     * @return
     */
    public boolean isHasChildren() {
    	return this.hasChildren;
    }
    
    public List<String> getChildren() {
        return children;
    }

    public void setChildren(final List<String> children) {
        this.children = children;
    }

    public List<Person> getBestFriends() {
    	return this.bestFriends;
    }
    
    @Override
    public String toString() {
        return "Person [name=" + name + ", married=" + married + ", spouse="
                + getSpouse() + ", children=" + children + "]";
    }
}
