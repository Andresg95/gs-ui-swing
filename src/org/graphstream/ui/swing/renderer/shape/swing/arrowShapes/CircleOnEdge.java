package org.graphstream.ui.swing.renderer.shape.swing.arrowShapes;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.geom.Vector2;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.Style;
import org.graphstream.ui.swing.Backend;
import org.graphstream.ui.swing.SwingDefaultCamera;
import org.graphstream.ui.swing.renderer.Skeleton;
import org.graphstream.ui.swing.renderer.shape.Connector;
import org.graphstream.ui.swing.renderer.shape.swing.baseShapes.AreaOnConnectorShape;
import org.graphstream.ui.swing.util.CubicCurve;
import org.graphstream.ui.swing.util.ShapeUtil;
import org.graphstream.ui.swing.util.AttributeUtils.Tuple;

public class CircleOnEdge extends AreaOnConnectorShape {
	Ellipse2D.Double theShape = new Ellipse2D.Double();

	@Override
	public void make(Backend backend, SwingDefaultCamera camera) {
		make( false, camera );
	}

	@Override
	public void makeShadow(Backend backend, SwingDefaultCamera camera) {
		make( true, camera );
	}
	
	private void make(boolean forShadow, SwingDefaultCamera camera) {
		if( theConnector.skel.isCurve() )
			makeOnCurve( forShadow, camera );
		else 
			makeOnLine(  forShadow, camera );
	}
	
	private void makeOnCurve(boolean forShadow, SwingDefaultCamera camera) {
		Tuple<Point2, Double> tuple = CubicCurve.approxIntersectionPointOnCurve( theEdge, theConnector, camera );
		Point2 p1 = tuple.x ;
		double t = tuple.y ;
		
		Style style  = theEdge.getStyle();
				
		Point3 p2 = CubicCurve.eval( theConnector.fromPos(), theConnector.byPos1(), theConnector.byPos2(), theConnector.toPos(), t-0.1f );
		Vector2 dir = new Vector2( p1.x - p2.x, p1.y - p2.y );
		dir.normalize();
		dir.scalarMult( theSize.x/2 );

		// Create a polygon.
		theShape.setFrame( (p1.x-dir.x())-(theSize.x/2), (p1.y-dir.y())-(theSize.y/2), theSize.x, theSize.y );			
	}

	private void makeOnLine(boolean forShadow, SwingDefaultCamera camera) {
		double off = ShapeUtil.evalTargetRadius2D( theEdge, camera ) + ((theSize.x+theSize.y)/4);
		Vector2 theDirection = new Vector2(
				theConnector.toPos().x - theConnector.fromPos().x,
				theConnector.toPos().y - theConnector.fromPos().y );
		
		theDirection.normalize();
		  
		double x = theCenter.x - ( theDirection.x() * off );
		double y = theCenter.y - ( theDirection.y() * off );
		//val perp = new Vector2( theDirection(1), -theDirection(0) )
				
		//perp.normalize
		theDirection.scalarMult( theSize.x );
		//perp.scalarMult( theSize.y )
		
		if( forShadow ) {
			x += shadowable.theShadowOff.x;
			y += shadowable.theShadowOff.y;
		}
		
		// Set the shape.	
		theShape.setFrame( x-(theSize.x/2), y-(theSize.y/2), theSize.x, theSize.y );
	}

	@Override
	public void render(Backend bck, SwingDefaultCamera camera, GraphicElement element, Skeleton skeleton) {
		Graphics2D g = bck.graphics2D();
		make( false, camera );
		strokable.stroke( g, theShape );
		fillable.fill( g, theShape, camera );
	}
	
	public double lengthOfCurve( Connector c ) {
		// Computing a curve real length is really heavy.
		// We approximate it using the length of the 3 line segments of the enclosing
		// control points.
		return ( c.fromPos().distance( c.byPos1() ) + c.byPos1().distance( c.byPos2() ) + c.byPos2().distance( c.toPos() ) ) * 0.75f;
	}

	@Override
	public void renderShadow(Backend bck, SwingDefaultCamera camera, GraphicElement element, Skeleton skeleton) {
		make( true, camera );
		shadowable.cast(bck.graphics2D(), theShape );
	}
	
}