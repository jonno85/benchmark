package com.example.benchmarkactivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class AnGraphic extends View {

	private static final float RADIUS = (float) 2.5;
	private static boolean INIT;
	private int Y_BOTTOM_SPACE=16;
	private int X_LEFT_SPACE=3;
	private int xLeft, xRight, yTop, yBottom, graphicHeight, graphicWidth, minutes, seconds;
	private int memTotal = 100;
	private String updateIntervalText = "Data Set"; // These strings will be afterwards updated with the right language.
	private Paint graphicBackgroudPaint, viewBackgroudPaint, circlePaint,
			textRecordingPaint, linesPaint, linesPaintLow, ratePaint, cPURestPPaint, memFreePaint,
			buffersPaint, cachedPaint, activePaint, inactivePaint, swapTotalPaint, dirtyPaint;
	private Paint[] colors;
	private Vector<String> memFree, buffers, cached, active, inactive, swapTotal, dirty;
	private Vector<Float> cPUAMP, cPURestP;
	
	private HashMap<String, LinkedList<DataContainer>> stats = null;
	private int textSize;
	private AttributeSet attrs;
	private Rect frame;
	private Rect rect1 = new Rect(0, yTop, xLeft, getHeight());
	private Rect rect2 = new Rect(0, yBottom, xRight, getHeight());
	private int mode = 0;

	private String hashKey = "";
	/**
	 * storage file
	 */
	private String statFile;
	private ObjectOutputStream	oos;
	private ObjectInputStream	ois;

	// This constructor must be specified when the view is loaded from a xml file, like in this case.
	public AnGraphic(Context context, AttributeSet attrs) {
		super(context, attrs);
		statFile = context.getFilesDir().toString() + "/statistics";
//		Log.e("NEW PATH", ""+statFile);
		this.attrs = attrs;
		INIT = true;
//		setAndRestore();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(INIT){
			setAttrs(attrs);
		}
		// Graphic background
		canvas.drawRect(frame, graphicBackgroudPaint);

		if(stats != null && !hashKey.equalsIgnoreCase("")){
			drawData(canvas);
		}

		// In Frame Legend
		canvas.drawText("| " + updateIntervalText+": "+MainActivity.SET, xRight-5, yTop+15, colors[0]);
		canvas.drawText("Priority:",xRight-150, yTop+15, colors[0]);
		canvas.drawText("0",		xRight-100, yTop+15, colors[0]);
		canvas.drawText("-2",		xRight-115, yTop+15, colors[3]);
		canvas.drawText("-4",		xRight-135, yTop+15, colors[2]);

		// Graphic background
		canvas.drawRect(rect1, viewBackgroudPaint);
		canvas.drawRect(rect2, viewBackgroudPaint);

		// Vertical edges
		canvas.drawLine(xLeft, yTop, xLeft, yBottom, linesPaint);
		canvas.drawLine(xRight, yBottom, xRight, yTop, linesPaint);

		// Horizontal edges
		canvas.drawLine(xLeft, yTop, xRight, yTop, linesPaint);
		canvas.drawLine(xLeft, yBottom, xRight, yBottom, linesPaint);

		// Vertical legend - Horizontal graphic grid lines
		if(mode == 0){ //ONE SHOT MODE, collect data on different scale
			canvas.drawText("time", xLeft-X_LEFT_SPACE, yTop, colors[4]);
			canvas.drawText("3",	xLeft-X_LEFT_SPACE, yTop+10, colors[4]);
			canvas.drawText("2.5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/6+5,	 colors[4]);
			canvas.drawText("2",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/3+5,	 colors[4]);
			canvas.drawText("1.5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/2+5,	 colors[4]);
			canvas.drawText("1",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/3*2+5,colors[4]);
			canvas.drawText("0.5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/6*5+5,colors[4]);
			canvas.drawText("0",	xLeft-X_LEFT_SPACE, yBottom,				 colors[4]);
			
			canvas.drawLine(xLeft, yTop+graphicHeight/6,	xRight, yTop+graphicHeight/6,	linesPaintLow);	//0.5
			canvas.drawLine(xLeft, yTop+graphicHeight/2,	xRight, yTop+graphicHeight/2,	linesPaint);		//1.5
			canvas.drawLine(xLeft, yTop+graphicHeight/6*2,	xRight, yTop+graphicHeight/6*2,	linesPaint);	//1.
			canvas.drawLine(xLeft, yTop+graphicHeight/6*4,	xRight, yTop+graphicHeight/6*4,	linesPaint);	//2.
			canvas.drawLine(xLeft, yTop+graphicHeight/6*5,	xRight, yTop+graphicHeight/6*5,	linesPaintLow);	//2.5
		} else {
			canvas.drawText("rate", xLeft-X_LEFT_SPACE, yTop,						colors[4]);
			canvas.drawText("10",	xLeft-X_LEFT_SPACE, yTop+10,					colors[4]);
			canvas.drawText("7.5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/4+5,		colors[4]);
			canvas.drawText("5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/2+5,		colors[4]);
			canvas.drawText("2.5",	xLeft-X_LEFT_SPACE, yTop+graphicHeight/4*3+5,	colors[4]);
			canvas.drawText("0",	xLeft-X_LEFT_SPACE, yBottom,					colors[4]);
			
			canvas.drawLine(xLeft, yTop+graphicHeight/4,	xRight, yTop+graphicHeight/4,	linesPaint);
			canvas.drawLine(xLeft, yTop+graphicHeight/2,	xRight, yTop+graphicHeight/2,	linesPaint);
			canvas.drawLine(xLeft, yTop+graphicHeight/4*3,	xRight, yTop+graphicHeight/4*3,	linesPaint);
			
		}
		// Horizontal legend
		canvas.drawText("Steps",xRight, yBottom+25, colors[4]);
		for (int n=0; n<=MainActivity.DIVISOR; n++){
			float step = graphicWidth/MainActivity.DIVISOR;
			canvas.drawLine(xRight-n*step,
							yTop,
							xRight-n*step,
							yBottom,
							linesPaint);
			canvas.drawText(((MainActivity.DIVISOR - n) * 10) + "'",
							xRight - n*step,
							yBottom + Y_BOTTOM_SPACE,
							colors[4]);
			
		}
	}

	/**
	 * It draws the line of the taken memory value vector. This action consumes quite system resources.
	 * 
	 * This method is only called from the onDraw() overrided method.
	 */
	private void drawLine(Vector<String> y, Canvas canvas, Paint paint) {
		if(y.size()>1){
			for(int m=0; m<(y.size()-1) && m<MainActivity.TOTAL_INTERVALS; m++) {
				canvas.drawLine(xRight-MainActivity.WIDTH_INTERVAL*m,
						yBottom-Integer.parseInt(y.elementAt(m))*graphicHeight/memTotal,
						xRight-MainActivity.WIDTH_INTERVAL*m-MainActivity.WIDTH_INTERVAL,
						yBottom-Integer.parseInt(y.elementAt(m+1))*graphicHeight/memTotal, paint);
			}
		}
	}

	/**
	 * It draws the line of the taken CPU usage value vector. This action consumes quite system resources.
	 * 
	 * This method is only called from the onDraw() overrided method.
	 */
	private void drawLineFloat(Vector<Float> y, Canvas canvas, Paint paint) {
		if(y.size()>1) for(int m=0; m<(y.size()-1) && m<MainActivity.TOTAL_INTERVALS; m++) {
			canvas.drawLine(xRight-MainActivity.WIDTH_INTERVAL*m,
					yBottom-y.elementAt(m)*graphicHeight/100,
					xRight-MainActivity.WIDTH_INTERVAL*m-MainActivity.WIDTH_INTERVAL,
					yBottom-y.elementAt(m+1)*graphicHeight/100, paint);
		}
	}

	private void drawData(Canvas canvas){
		float y = 0;
		float x = 0;
		DataContainer c;
		LinkedList<DataContainer> list = stats.get(hashKey);
		if(list != null){
			Iterator<DataContainer>it = list.descendingIterator();
			int counter = 1;
			while (it.hasNext()) {
				c = it.next();
				x = (counter * (float)(graphicWidth/MainActivity.SET)) + xLeft;
				counter++;
				if(mode != 0){
					y = (graphicHeight - ((float)(c.getRate() * graphicHeight)/10))+yTop;
				}else{
					y = (graphicHeight - (float)(((c.getTimeGap()/1000) * graphicHeight)/3000))+yTop;
				}
				canvas.drawCircle(x, y, RADIUS, getColor(c.getPrio()));
			}
		} else {
			canvas.drawText("No value stored", graphicWidth/2, graphicHeight/2, getColor(0));
		}
	}

	/**
	 * retrieve different color based on priority int
	 * @param prio
	 * @return
	 */
	private Paint getColor(int prio) {
		Paint color = null;
		switch(prio){
		case 0:
			color = colors[0];
			break;
		case -2:
			color = colors[3];
			break;
		case -4:
			color = colors[2];
			break;
		default:
			color = colors[0];
		}
		return color;
	}

	/**
	 * It initializes all the size variables, Paint objects and another stuff the first time
	 * the graphic is drawn or when the screen size change, as for example, the screen orientation changes.
	 * Like this we does not uselessly recalculate variables every time the graphic is drawn.
	 * 
	 * This method is only called once from the onDraw() overrided method if the INITIALICEGRAPHIC flag is true.
	 */
	private void setAttrs(AttributeSet attrs) {
		
		if(attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.graph, 0, 0);
			textSize				= a.getInt(R.styleable.graph_text_size, 12);
			graphicBackgroudPaint	= getPaint(a.getColor(R.styleable.graph_graph_background,Color.BLACK),		Paint.Align.CENTER, textSize, false);
			viewBackgroudPaint		= getPaint(a.getColor(R.styleable.graph_view_background, Color.TRANSPARENT),Paint.Align.CENTER, textSize, false);
			linesPaint				= getPaint(a.getColor(R.styleable.graph_lines_color,	 Color.BLUE),		Paint.Align.CENTER, textSize, false);
			linesPaintLow			= getPaint(a.getColor(R.styleable.graph_lines_color_low, Color.rgb(0,0,150)),	Paint.Align.CENTER, textSize, false);
			
			a.recycle();
		}

		xLeft	= (int)(getWidth()	*0.07);
		xRight	= (int)(getWidth()	*0.99);
		yTop	= (int)(getHeight()	*0.10);
		yBottom	= (int)(getHeight()	*0.80);

		frame = new Rect(xLeft, yTop, xRight, yBottom);

		graphicWidth	= xRight - xLeft;
		graphicHeight	= yBottom - yTop;

		circlePaint = getPaint(Color.RED, Paint.Align.CENTER, textSize, false);
//		linesPaint = getPaint(Color.parseColor(MainActivity.LINES_COLOR), Paint.Align.CENTER, textSize, false);
		colors = new Paint[5];
		colors[0] = getPaint(Color.GREEN,		Paint.Align.RIGHT, textSize, true);	//text GREEN
		colors[1] = getPaint(Color.BLUE,		Paint.Align.RIGHT, textSize, true); //text BLUE
		colors[2] = getPaint(Color.RED,		Paint.Align.RIGHT, textSize, true); //text RED
		colors[3] = getPaint(Color.YELLOW,	Paint.Align.RIGHT, textSize, true); //text YELLOW
		colors[4] = getPaint(Color.BLACK,		Paint.Align.RIGHT, textSize, true); //text BLACK
		
//		textRecordingPaint = getPaint(Color.LTGRAY, Paint.Align.LEFT, textSize, true);
//		ratePaint = getPaint(R.color.cpuamp, Paint.Align.CENTER, textSize, false);
//		cPURestPPaint = getPaint(R.color.cpurestppaint, Paint.Align.CENTER, textSize, false);
//		memFreePaint = getPaint(Color.GREEN, Paint.Align.CENTER, textSize, false);
//		buffersPaint = getPaint(Color.BLUE, Paint.Align.CENTER, textSize, false);
//		cachedPaint = getPaint(Color.CYAN, Paint.Align.CENTER, textSize, false);
//		activePaint = getPaint(Color.YELLOW, Paint.Align.CENTER, textSize, false);
//		inactivePaint = getPaint(Color.MAGENTA, Paint.Align.CENTER, textSize, false);
//		swapTotalPaint = getPaint(R.color.swaptotalpaint, Paint.Align.CENTER, textSize, false);
//		dirtyPaint = getPaint(R.color.dirtypaint, Paint.Align.CENTER, textSize, false);

		if(stats == null){
			setAndRestore();
		}

		INIT = false;
	}

	/**
	 * It initializes all the size variables, Paint objects and another stuff the first time
	 * the graphic is drawn or when the screen size change, as for example, the screen orientation changes.
	 * Like this we does not uselessly recalculate variables every time the graphic is drawn.
	 * 
	 * This method is only called from the initializeGraphic() method.
	 */
	private Paint getPaint(int color, Paint.Align align, int textSize, boolean b) {
		Paint p = new Paint();
		p.setColor(color);
		p.setTextSize(textSize);
		p.setTextAlign(align);
		p.setAntiAlias(b);
		return p;
	}

	/**
	 * associate to an internal variable all the values to show on the graph
	 * @param stats
	 * @param myContext
	 */
	void setLinkedLists(HashMap<String, LinkedList<DataContainer>> stats, Context myContext) {
		this.stats = stats;
		updateIntervalText = myContext.getString(R.string.graphic_update_interval);
	}

	public void saveInfoToStorage(){
		try {
			oos = new ObjectOutputStream(new FileOutputStream(statFile));
			oos.writeObject(stats);
			if(ois != null){
				ois.close();
			}
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setAndRestore(){
		try {
			ois		= new ObjectInputStream(new FileInputStream(statFile));
			stats	= (HashMap<String, LinkedList<DataContainer>>) ois.readObject();
		} catch (ClassNotFoundException ex) {;
			ex.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(stats == null){
				initStats();
			}
		}
	}

	private void initStats(){
		stats = new HashMap<String, LinkedList<DataContainer>>();
		for(int i=0; i<Widget.values.length; i++){
			for(int j=0; j<MainActivity.sourcesItems.length; j++){
				stats.put(""+Widget.values[i]+j, new LinkedList<DataContainer>());
			}
		}
	}

	public String addDataContainer(int key, int mode, DataContainer dc){
		DataContainer c;
		float avgRate	 = 0;
		float avgPackets = 0;
		float avgLoss	 = 0;
		float index		 = 1;
		this.mode	= mode;
		hashKey		= ""+key+mode;

		LinkedList<DataContainer> list = stats.get(hashKey);
		if(list == null){
			list = new LinkedList<DataContainer>();
		}
		Iterator<DataContainer> i = list.iterator();
		while(i.hasNext()){
			c			= i.next();
			avgRate		+= c.getRate();
			avgPackets	+= c.getPackets();
			avgLoss		+= c.getLoss();
			index++;
		}
		avgRate		+= dc.getRate();
		avgLoss		+= dc.getLoss();
		avgPackets	+= dc.getPackets();
		avgRate		/= index;
		avgPackets	/=index;
		avgLoss		/=index;
		dc.setAvgRate(avgRate);
		dc.setAvgPackets(avgPackets);
		dc.setAvgLoss(avgLoss);
		list.add(dc);

		String opts = (mode !=0)? "\nRate: " 	+ avgRate		+
								  "\nPackets: "	+ avgPackets	+
								  "\nLost: "	+ avgLoss		
								  : "";
		String report = "AVG (Set "+index+"):" + opts + 
						"\ncpu usage: " + dc.getCpuLoad() + " clock ticks ";
		
		return report;
	}
}