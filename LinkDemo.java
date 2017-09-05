//将node类定义在Link类之中，作为内部类，避免让用户直接接触到
class Link <T>{
	private Node root ;
	
	private int count = 0 ;
	private T[] retArray ;
	private int foot ;
	// 链表添加数据
	public void add(T data) {
		if (data == null ){
			return ;
		}
		else{
			Node node = new Node(data) ;
			if( this.root == null ){
				this.root = node ;
			}else{
				this.root.addNode(node) ;
			}
			this.count ++ ;
		}
	}
	// 链表大小
	public int size() {
		return this.count ;
	}
	// 链表判空
	public boolean isEmpty() {
		return this.count == 0;
	}
	// 链表删除数据
	public void remove(T data) {
		if ( this.contains(data)) {
			if (data.equals(this.root)){
				this.root = this.root.Next ; 	//	根节点，直接删除
			}else {
				this.root.Next.removeNode(this.root,data) ;
			}
			this.count -- ;
		}
		else {
			System.out.println("没有查找到要删除的数据！") ;
		}
	}
	// 是否包含指定数据
	public boolean contains(T data) {
		if(this.root == null || data == null ){
			return false ;
		}else{
			return this.root.contains(data) ;
			}
	}
	
	// 打印链表内容
	public void print(){
		System.out.println("总共" + this.size() + "结点！") ;
		if(this.root != null){
			this.root.printNode() ;
		}
	}
	
	// 根据索引取得数据
	public T get(int index) {
		if(index > this.size() || index < 0){
			return null ;
		}else {
			return	this.root.getNode(index) ;
		}
	}
	/***  此处泛型数组定义错误，java不支持泛型数组
	// ToArray
	public T[] toArray() {
		if(this.root == null) {
			return null ;
		}
		this.foot = 0 ;
		this.retArray = (T[])new Object[this.count] ;
		this.root.toArrayNode() ;
		return this.retArray ;
	}
	*/
	// 清空
	public void clear(){
		this.root = null ;
		this.count = 0 ;
	}
	
	
	/****************** 以下为内部类的定义： NOde类*******/
	class Node {
	private T data ;
	private Node Next ;
	
	public Node(T data){
		this.data = data ;
		this.Next = null ;
	}
	
	public void addNode(Node node) {
		if (this.Next == null) {
			this.Next = node ;
		}else{
			this.Next.addNode(node) ;
		}
	}
	
	public boolean contains(T data) {
		
		if(this.data.equals(data)) {
			return true ;
		}else{
			if(this.Next != null){
				return this.Next.contains(data) ;
			}else{
				return false ;
			}
		}
	}
	
	public void removeNode(Node pre,T data) {
		if (pre.Next.data.equals(data)){		// if(data.equals(this.data))
			pre.Next = pre.Next.Next ;			// pre.next = this.next ;
		}
		else {
			pre = pre.Next ;
			pre.Next.removeNode(pre,data) ;		// this.next.removeNode(this,data) ;
		}
	}
	
	// 这里在Link类中已经判断了当前节点不为空，所有可以先输出，在判断下一个节点
	// 不能先 进行 this.Next.printNode()，在进行this.Next == null 的判断，这样会出现空指针异常
	public void printNode(){
		// System.out.println(this.data) ;
		System.out.println(this.data.toString());
		if (this.Next != null) {
			this.Next.printNode() ;
		}
	}
	
	public T getNode(int index){
		if (index == 0){
			return this.data ;
		}
		else{
			return this.Next.getNode(--index) ;
		}
		
	}
	
	public void toArrayNode() {
		Link.this.retArray[Link.this.foot++] = this.data ;
		if(this.Next != null){
			this.Next.toArrayNode() ;	
		}
	}

}
	
	/*************************************************/
}


class Student {
	private String name ;
	private int num ;
	private String major ;
	
	public Student(String name,int num,String major){
		this.name = name ;
		this.num = num ;
		this.major = major ;
	}
	
	public boolean equals(Object obj) {
		if(this == obj) {
			return true ;
		}
		if (obj instanceof Student){
			Student stu = (Student)obj ;
			if(this.name.equals(stu.name) && this.num == stu.num && this.major.equals(stu.major)){
				return true ;
			}else{
				return false ;
			}
		}else{
			return false ;
		}
	}
	
	public String toString(){
		return "姓名：" + this.name + ", 学号：" + this.num + ", 专业：" + this.major ;
	}
}
public class LinkDemo {
	public static void main(String args[]) {
	
		
		Link<String> link_string = new Link<String>() ;
		Link<Student> link_student = new Link<Student>() ;
		/* 使用链表操作String
		// 增加数据
		link_string.add("AAA") ;
		link_string.add("BBB") ;
		link_string.add("CCC") ;
		link_string.add("DDD") ;
		// 打印当前链表
		link_string.print() ;
		// 按索引查找数据
		System.out.println(link_string.get(2)) ;
		System.out.println(link_string.get(0)) ;
		// 删除链表数据
		link_string.remove("BBB") ;
		link_string.remove("BBB") ;
		link_string.remove("DDD") ;
		// 打印删除后的链表
		link_string.print() ;
		*/
		
		/** 泛型化后的链表，操作Student类对象 **/
		Student stuA = new Student("小张",1001,"Math") ;
		Student stuB = new Student("小王",1002,"Chinese") ;
		Student stuC = new Student("小赵",1003,"SoftWare") ;
		link_student.add(stuA) ;
		link_student.add(stuB) ;
		link_student.add(stuC) ;
		link_student.print() ;
		System.out.println("第二个节点是："  + link_student.get(2)) ;
		link_student.add(new Student("小李",1005,"Chinese")) ;
		link_student.add(new Student("小原",1006,"Computer")) ;
		link_student.print() ;
		link_student.remove(stuB) ;
		link_student.remove(new Student("小李",1005,"Chinese")) ;
		link_student.print() ;
		
		
	}
}
