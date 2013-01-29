//Custom list for quick head/tail removal
//Two way linked list, forwards and backwards

public class MyList<K> {
	Node<K> head;
	Node<K> tail;
	int size;
	
	@SuppressWarnings("hiding")
	class Node<K> {
		K v;
		Node<K> next;
		Node<K> prev;
		
		public Node(K v, Node<K> n, Node<K> p) {
			this.v = v;
			next = n;
			prev = p;
		}
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	//add to tail
	public void enqueue (K v) {
		Node<K> n = new Node<K>(v, null, tail);
		
		if (head == null) {
			head = n;
			tail = n;
		}
		else {
			tail.next = n;
			tail = n;
		}
		
		size++;
	}
	
	//Assumes queue is not empty
	public K removeHead() {
		K result;
		
		if (size == 1) {
			result = tail.v;
			tail = null;
		}
		else {
			result = head.v;
			head = head.next;
			head.prev = null;		
		}
		size--;
		
		return result;
	}
	
	//Assumes queue is not empty
	public K removeTail() {
		K result;
		
		if (size == 1) {
			result = head.v;
			head = null;
		}
		else {
			result = tail.v;
			tail = tail.prev;
			tail.next = null;		
		}
		size--;
		
		return result;
	}
}
