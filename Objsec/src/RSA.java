import java.math.BigInteger;
import java.util.Random;


public class RSA {
	private int[] primes;
	private Random r;
	private BigInteger phi;
	private int e;
	public RSA(){
		primes=new int[100];
		r = new Random();
		generatePrimes();
	}
	
	private void generatePrimes(){
		int c=0;
		int i=1;
		while(c<100){
			boolean isPrime = true;
			for(int j=2; j<i; j++){
				if(i%j==0){
					isPrime=false;
					break;
				}
			}
			if(isPrime){
				if(i>1087){
					primes[c]=i;
					c++;
				}
			}
			i++;
		}
	}
	
	public void printPrimes(){
		for(int i=0; i<100; i++){
			System.out.print(primes[i] + " ");
		}
	}
	
//	public int generateN(){
//		int i=r.nextInt(100);
//		int x=r.nextInt(100);
//		while(x==i){
//			x=r.nextInt(100);
//		}
//		phi = (primes[i]-1)*(primes[x]-1);
//		return primes[i]*primes[x];
//	}
	
	public BigInteger generateN(){
		BigInteger p = BigInteger.probablePrime(90, new Random());
		BigInteger q = BigInteger.probablePrime(90, new Random());
		BigInteger ptemp = p.subtract(new BigInteger("1"));
		BigInteger qtemp = q.subtract(new BigInteger("1"));
		phi = ptemp.multiply(qtemp);
		//System.out.println("P: " + p + "\nq: " + q + "\nptemp: " + ptemp + "\nqtemp: " + qtemp + "\nphi: " + phi);
		return p.multiply(q);
	}
	
	public int generateE(){
		e = r.nextInt(21);
		e=e+2;
		BigInteger result = gcd(e,phi);
		BigInteger temp = new BigInteger("1");
		while(!result.equals(temp)){
			e = r.nextInt(21);
			e=e+2;
			result=gcd(e,phi);
		}
		return e;
	}
	
	public BigInteger getPhi(){
		return phi;
	}
	
//	private int gcd(int a,BigInteger b){
//		int t;
//		while(b!=0){
//			t=b;
//			b=a%b;
//			a=t;
//		}
//		return a;
//	}
	
	public BigInteger gcd(int a, BigInteger b){
		return b.gcd(new BigInteger(""+a));
	}
	
//	public int generateD(){
//		int t=phi;
//		int told=phi;
//		int r=phi;
//		int rold=phi;
//		int td=e;
//		int rd=1;
//		
//		
//		while(t!=1){
//			t=told/td;// 40/7=5
//			r=t*rd;// 5*1=5
//			t=t*td;// 5*7=35
//			t=told-t;//40-35=5
//			r=rold-r;//40-5=35
//			while(r<0){
//				r=r+phi;
//			}
//			told=td;
//			rold=rd;
//			td=t;
//			rd=r;
//		}
//		return r;
//	}
	
	
	public BigInteger generateD(){
		BigInteger t=phi;
		BigInteger told=phi;
		BigInteger r=phi;
		BigInteger rold=phi;
		BigInteger td=new BigInteger(""+e);
		BigInteger rd=new BigInteger("1");
		
		BigInteger temp = new BigInteger("1");
		BigInteger temp0 = new BigInteger("0");
		while(!t.equals(temp)){
			t=told.divide(td);//told/td;// 40/7=5
			r=t.multiply(rd);//t*rd;// 5*1=5
			t=t.multiply(td);//t*td;// 5*7=35
			t=told.subtract(t);//told-t;//40-35=5
			r=rold.subtract(r);//rold-r;//40-5=35
			
			
			while(r.compareTo(temp0)<0){
				r=r.add(phi);//r+phi;
			}
			told=td;
			rold=rd;
			td=t;
			rd=r;
		}
		return r;
	}
	
	public BigInteger generateInverse(BigInteger value,BigInteger e){
		BigInteger t=value;
		BigInteger told=value;
		BigInteger r=value;
		BigInteger rold=value;
		BigInteger td=e;
		BigInteger rd=new BigInteger("1");
		
		BigInteger temp = new BigInteger("1");
		BigInteger temp0 = new BigInteger("0");
		while(!t.equals(temp)){
			t=told.divide(td);//told/td;// 40/7=5
			r=t.multiply(rd);//t*rd;// 5*1=5
			t=t.multiply(td);//t*td;// 5*7=35
			t=told.subtract(t);//told-t;//40-35=5
			r=rold.subtract(r);//rold-r;//40-5=35
			
			
			while(r.compareTo(temp0)<0){
				r=r.add(value);//r+phi;
			}
			told=td;
			rold=rd;
			td=t;
			rd=r;
		}
		return r;
	}
	
//	public int generateD2(){
//		int m=phi;
//		int m0=m,t,q;
//		int x0= 0,x1=1;
//		int a=e;
//		if(phi==1){
//			return 0;
//		}
//		
//		while(a>1){
//			q=a/m;
//			t=m;
//			m=a%m;
//			a=t;
//			t=x0;
//			x0=x1-q*x0;
//			x1=t;
//		}
//		if(x1<0){
//			x1 += m0;
//		}
//		return x1;
//	}
	
//	public int encrypt(int m,int e,int n){
//		long t = (long)Math.pow(m, e);
//		int temp = (int)(((t%n)+n)%n);
//		return temp;
//	}
	
//	public int encrypt(String m,int e,int n){
//		BigInteger v = new BigInteger(m,16);
//		System.out.println("vb4:" + v);
////		BigInteger t = v.pow(e);
////		v=v.mod(new BigInteger(""+n));
//		v=v.modPow(new BigInteger(""+e), new BigInteger(""+n));
//		System.out.println("m: " + m +"\ne: " + e +"\nn: " + n + "\nv: " + v);
//		return v.intValue();
//	}
	
	public BigInteger verifysign(BigInteger m,int e, BigInteger n){
		return m.modPow(new BigInteger(""+e), n);
	}
	
	public BigInteger encrypt(String m,int e,BigInteger n){
		BigInteger v = new BigInteger(m,16);
		v=v.modPow(new BigInteger(""+e), n);
		return v;
	}
	
//	public int decrypt(int m, int d, int n){
//		long base = m;
//		long result=1;
//		System.out.println("MOD--------------------------");
//		System.out.println("m: " + m);
//		System.out.println("d: " + d);
//		System.out.println("n: " + n);
//		System.out.println("MOD--------------------------");
//		while(d>0){
//			if(d%2!=0){
//				result = (result*base)%n;
//			}
//			base = (base*base)%n;
//			d=d/2;
//		}
//		return (int)result;
//	}
	
	public BigInteger decrypt(BigInteger m, BigInteger d, BigInteger n){

//		System.out.println("MOD--------------------------");
//		System.out.println("m: " + m);
//		System.out.println("d: " + d);
//		System.out.println("n: " + n);
//		System.out.println("MOD--------------------------");

		return m.modPow(d, n);
	}
	
	public BigInteger sign(BigInteger m, BigInteger d, BigInteger n){
		return m.modPow(d, n);
	}
	
	public int mod(int a,int m){
		return (((a%m)+m)%m);
	}
}
