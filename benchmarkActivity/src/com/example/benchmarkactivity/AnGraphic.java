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

	private static final float RADIUS = (float) 2;
	private static boolean INIT;
	private int Y_BOTTOM_SPACE=16;
	private int X_LEFT_SPACE=3;
	private int xLeft, xRight, yTop, yBottom, graphicHeight, graphicWidth;
	private Paint graphicBackgroudPaint, viewBackgroudPaint, linesPaint, linesPaintLow;
	private Paint[] colors;
	
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
			printLogScale(canvas);
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
		// In Frame Legend
		canvas.drawText("| Data Set: "+MainActivity.SET, xRight-5, yTop+15, colors[0]);
		canvas.drawText("Priority:",xRight-150, yTop+15, colors[0]);
		canvas.drawText("0",		xRight-100, yTop+15, colors[0]);
		canvas.drawText("-2",		xRight-115, yTop+15, colors[3]);
		canvas.drawText("-4",		xRight-135, yTop+15, colors[2]);

		// Horizontal legend
		canvas.drawText("Steps",xRight, yBottom+25, colors[4]);
		for (int n=0; n<=MainActivity.DIVISOR; n++){
			float step = graphicWidth/MainActivity.DIVISOR;
			canvas.drawLine(xRight - n * step,
							yTop,
							xRight - n * step,
							yBottom,
							linesPaint);
			canvas.drawText(((MainActivity.DIVISOR - n) * 10) + "'",
							xRight - n * step,
							yBottom + Y_BOTTOM_SPACE,
							colors[4]);
			
		}

		if(stats != null && !hashKey.equalsIgnoreCase("")){
			drawData(canvas);
		}
	}

	private void drawData(Canvas canvas){
		float prevX = 0, prevY = 0, y = 0, x = 0;
		DataContainer c;
		LinkedList<DataContainer> list = stats.get(hashKey);
		if(list != null){
			Iterator<DataContainer>it = list.descendingIterator();
			int counter = 1;
			while (it.hasNext()) {
				c = it.next();
				x = (counter * (float)(graphicWidth/MainActivity.SET)) + xLeft;

				if(mode != 0){
					y = (graphicHeight - ((float)(c.getRate() * graphicHeight)/MainActivity.LISCALE))+yTop;
				}else{
					//Log to value scaled to uS
					float y1 = (float) Math.log10((int)c.getTimeGap()/1000);
					//proportion to half scale
					y1 = (y1 * (graphicHeight/2));
					//move to relative graph parameter
					y = (2 * graphicHeight + yTop - y1);
				}
				if(counter == 1){
					prevX = x;
					prevY = y;
				}
				canvas.drawCircle(x, y, RADIUS, getColor(c.getPrio()));
				canvas.drawLine(prevX, prevY, x, y, colors[0]);
				prevX = x;
				prevY = y;
				counter++;
			}
		} else {
			canvas.drawText("No value stored", (graphicWidth + yTop)/2, (graphicHeight + xLeft)/2, getColor(0));
		}
	}

	/**
	 * retrieve different color based on priority int number
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

		colors = new Paint[5];
		colors[0] = getPaint(Color.GREEN,		Paint.Align.RIGHT, textSize, true);	//text GREEN
		colors[1] = getPaint(Color.BLUE,		Paint.Align.RIGHT, textSize, true); //text BLUE
		colors[2] = getPaint(Color.RED,		Paint.Align.RIGHT, textSize, true); //text RED
		colors[3] = getPaint(Color.YELLOW,	Paint.Align.RIGHT, textSize, true); //text YELLOW
		colors[4] = getPaint(Color.BLACK,		Paint.Align.RIGHT, textSize, true); //text BLACK

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
	}

	/**
	 * save stats file to serialization file and close file objects
	 */
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

	/**
	 * probe if there is an already stats object from serialization file or init a new one
	 */
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

	/**
	 * Initialize stats file with its internal structure
	 */
	private void initStats(){
		stats = new HashMap<String, LinkedList<DataContainer>>();
		for(int i=0; i<Widget.values.length; i++){
			for(int j=0; j<MainActivity.sourcesItems.length; j++){
				stats.put(""+Widget.values[i]+j, new LinkedList<DataContainer>());
			}
		}
	}

	/**
	 * add new element on internal stats file and calculate the new avgs values updated
	 * @param key
	 * @param mode
	 * @param dc
	 * @return
	 */
	public String addDataContainer(int key, int mode, DataContainer dc){
		DataContainer c;
		float avgRate	 = 0;
		float avgPackets = 0;
		float avgLoss	 = 0;
		float index		 = 1;

		this.mode	= mode;
		hashKey		= "" + key + mode;

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

		String opts = (mode !=0)? "\nRate: " 	+ avgRate	 +
								  "\nPackets: "	+ avgPackets +
								  "\nLost: "	+ avgLoss
								  : "";
		String report = "AVG (Set "+index+"):" + opts +
						"\ncpu usage: " + dc.getCpuLoad() + " clock ticks ";
		
		return report;
	}

	private void printLogScale(Canvas canvas){
		
		canvas.drawText("log",		xLeft-X_LEFT_SPACE, yTop - 20, colors[4]);
		canvas.drawText("10^3 ns",	xLeft-X_LEFT_SPACE, yTop - 10, colors[4]);
		
		for(int i=10; i>0; i--){
			float x = (float)(yTop+graphicHeight - ((graphicHeight/2) * Math.log10(i)));
			if(i%2==0 || i==1){
				canvas.drawText(""+i+"00",  xLeft-X_LEFT_SPACE, x+5, colors[4]);
				canvas.drawLine(xLeft - 2, x, xRight, x, colors[2]);
			} else {
				canvas.drawLine(xLeft, x, xRight, x, colors[2]);
			}
		}
		for(int i=10; i>1; i--){
			float x = (float)(yTop+graphicHeight/2 - ((graphicHeight/2) * Math.log10(i)));
			if(i%2==0){
				canvas.drawText(""+i+"000",  xLeft-X_LEFT_SPACE, x+5, colors[4]);
				canvas.drawLine(xLeft - 2, x, xRight, x, colors[2]);
			}else{
				canvas.drawLine(xLeft, x, xRight, x, colors[2]);
			}
		}
	}
}