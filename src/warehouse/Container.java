package warehouse;

public class Container {
	private int id;
	
	public Container(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
    //Добавлены методы hashCode и equals для помещения в коллекции
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Container container = (Container) o;

		return id == container.id;

	}

	@Override
	public int hashCode() {
		return id*31;
	}
}
