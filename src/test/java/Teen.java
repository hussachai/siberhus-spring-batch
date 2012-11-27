

public class Teen {
	
	static String test(){
		try{
//			throw new RuntimeException("hello");
			return "Hello";
		}finally{
			return "Swallow";
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println(test());
	}
}
