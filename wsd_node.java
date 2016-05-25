package libsvm;

public class wsd_node extends svm_node
{
	public String token ;
	
	public void wsd_node()
	{
		token = null ;
	}
	public String toString()
	{
		return token ;
	}
}
