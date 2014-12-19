package com.link.platform.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import com.link.platform.MainApplication;
import com.link.platform.R;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmilyManager {

	private static SmilyManager instance;
	
	private HashMap<String, Integer> mSmileyToIndex;

	private Pattern mPattern;
	
	private Context ctx;
	
	private SmilyManager() {
		
		this.ctx = MainApplication.getInstance();
		
		init();
		
	}
	
	private void init(){
		mSmileyToIndex = new HashMap<String, Integer>();
		for(int i=0,length=flags.length;i<length;i++){
			mSmileyToIndex.put(flags[i], i);
		}
		mPattern = buildPattern();
	}
	
	private Pattern buildPattern(){
        StringBuilder patternString = new StringBuilder(flags.length * 3);
        patternString.append('(');
        for (String s : flags) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }


	public static SmilyManager getInstance() {
		if (instance == null) {
			instance = new SmilyManager();
		}
		return instance;
	}

    public String getShortCut(int i){
        return flags[i];
    }
	
	public CharSequence getSmilySpan(CharSequence text) {
		
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int index = mSmileyToIndex.get(matcher.group());

	        Drawable drawable = ctx.getResources().getDrawable(R.drawable.eaa + index);
            final int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, ctx.getResources().getDisplayMetrics());
            drawable.setBounds(0, 0, size, size);
            ImageSpan imgSpan = new ImageSpan(drawable);
            builder.setSpan(imgSpan,
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

	public static final String[] flags = 
	    {
	        "/:^_^",
	        "/:^$^",
	        "/:Q",
	        "/:815",
	        "/:809",
	        "/:^O^",
	        "/:081",
	        "/:087",
	        "/:086",
	        "/:H",
	        "/:012",
	        "/:806",
	        "/:b",
	        "/:^x^",
	        "/:814",
	        "/:^W^",
	        "/:080",
	        "/:066",
	        "/:807",
	        "/:805",
	        "/:071",
	        "/:072",
	        "/:065",
	        "/:804",
	        "/:813",
	        "/:818",
	        "/:015",
	        "/:084",
	        "/:801",
	        "/:811",
	        "/:?",
	        "/:077",
	        "/:083",
	        "/:817",
	        "/:!",
	        "/:068",
	        "/:079",
	        "/:028",
	        "/:026",
	        "/:007",
	        "/:816",
	        "/:\'\"\"",
	        "/:802",
	        "/:027",
	        "/:(Zz...)",
	        "/:*&*",
	        "/:810",
	        "/:>_<",
	        "/:018",
	        "/:>O<",
	        "/:020",
	        "/:044",
	        "/:819",
	        "/:085",
	        "/:812",
	        "/:\"",
	        "/:>M<",
	        "/:>@<",
	        "/:076",
	        "/:069",
	        "/:O=O",	//"/:O" smile_60.png
	        "/:067",
	        "/:043",
	        "/:P",
	        "/:808",
	        "/:>W<",
	        "/:073",
	        "/:008",
	        "/:803",
	        "/:074",
	        "/:O",    //"/:O=O" smile_70.png
	        "/:036",
	        "/:039",
	        "/:045",
	        "/:046",
	        "/:048",
	        "/:047",
	        "/:girl",
	        "/:man",
	        "/:052",
	        "/:(OK)",
	        "/:8*8",
	        "/:)-(",
	        "/:lip",
	        "/:-F",
	        "/:-W",
	        "/:Y",
	        "/:qp",
	        "/:$",
	        "/:%",
	        "/:(&)",
	        "/:@",
	        "/:~B",
	        "/:U*U",
	        "/:clock",
	        "/:R",
	        "/:C",      
	        "/:plane",
	        "/:075"
	    };

}
