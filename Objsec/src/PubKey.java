import java.math.BigInteger;


public class PubKey {
	public BigInteger n;
	public int e;
	
	PubKey(BigInteger n,int e){
		this.n=n;
		this.e=e;
	}
}
