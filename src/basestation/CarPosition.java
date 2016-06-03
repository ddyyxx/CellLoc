package basestation;

public class CarPosition implements Comparable <CarPosition> {
	public double lat,lng,dis;
	public CarPosition(){}
	public CarPosition(double x,double y,double len){
			this.lat=x;
			this.lng=y;
			this.dis=len;
	}
	public double Fabs(double x){
		return x>0?x:-x;
	}
	public boolean issame(CarPosition other){
		return Fabs(this.lat-other.lat)< 1e-8 && Fabs(this.lng-other.lng)< 1e-8;
	}
	public int compareTo(CarPosition other) {
		return this.dis<other.dis?-1:1;
	}
	public void print(){
		System.out.println(this.lat+" "+this.lng);
	}
}
