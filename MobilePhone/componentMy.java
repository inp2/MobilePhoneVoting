/*
    A Simple Example--Authentication Component.
    To Create a Component which works with the InterfaceServer,
    the interface ComponentBase is required to be implemented.
    
    interface ComponentBase is described in InterfaceServer.java.
    
    Enjoyed by flying, Jan 28,2004
    Any comments, please email:
       flying@cs.pitt.edu   
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class componentMy implements ComponentBase
{
    
    private final int init=0;
    private final int success=1;
    private final int failure=2;
    private int state;

	private final String delim = "$$$";
	private String vcAddress = "127.0.0.1";
	private Socket component;
	private Socket client;

	private Socket s;
	private PrintWriter pr;
	private BufferedReader br;
	private InputStream inStream;
	private OutputStream outStream;
	private String msgReceived;
	private String msgSent;
	private String head;
	private StringTokenizer strTok;
    
    public componentMy(String vcAddr, Socket comp, Socket cl)
	{
		vcAddress = vcAddr;
		component = comp;
		client = cl;
		s = null;
    	state = init;
    }
    
	public void set(Socket comp)
	{
		component = comp;
	}
    private void doAuthentication(String first, String last, String passwd, String isTeacher)
	{
		KeyValueList kvResult = new KeyValueList();
		MsgEncoder mEncoder= new MsgEncoder();

		try
		{
			s = new Socket (vcAddress, 7999);

			inStream = s.getInputStream();
			outStream = s.getOutputStream();             
			
			pr = new PrintWriter (outStream, true);
			br = new BufferedReader(new InputStreamReader(inStream));    
			
			msgReceived = br.readLine();
			strTok = new StringTokenizer(msgReceived, delim);
			head = strTok.nextToken();
			int code;
			if(isTeacher.equalsIgnoreCase("true"))
				code = 2;
			else
				code = 1;
			msgSent = head + delim + code + delim + first + delim + last + delim + passwd;

			pr.println (msgSent);

			msgReceived = br.readLine();
			strTok = new StringTokenizer(msgReceived, delim);
			
			int countTokens = strTok.countTokens();
			String arr [] = new String[countTokens];
			String token;
			kvResult.addPair("MsgID", "2");
			kvResult.addPair("Description", "List of available courses");
			for (int i = 0; i < countTokens; i++)
			{
				token = strTok.nextToken();
				arr[i] = new String(token);
				if (i > 1)
					kvResult.addPair("Course" + (i-2), arr[i]);
			}

			if (countTokens > 2 && arr[1].equals("1") )
				state = success;
			else
				state = failure;

			mEncoder.sendMsg(kvResult, client.getOutputStream());
                        mEncoder.sendMsg(kvResult, component.getOutputStream()); /* MODIFIED */

		}
		catch (Exception e)
		{
		}	      
    }

	public void selectCourseNS (String course)
	{
		KeyValueList kvResult = new KeyValueList();
		MsgEncoder mEncoder= new MsgEncoder();

		try
		{
			msgSent = head + delim + course;
			pr.println (msgSent);

			msgReceived = br.readLine();
			strTok = new StringTokenizer(msgReceived, delim);
			int countTokens = strTok.countTokens();

			kvResult.addPair("MsgID", "1");
			kvResult.addPair("Description", "Result of authentication");
			for (int i = 0; i < countTokens; i++)
			{
				switch (i)
				{
					case 1:
						if(!(strTok.nextToken()).equals("2"))
						countTokens = 0;
						break;
					case 2:
						kvResult.addPair("ServerIP", strTok.nextToken());
						break;
					case 3:
						kvResult.addPair("ServerPort", strTok.nextToken());
						break;
					case 4:
						kvResult.addPair("ImagePort", strTok.nextToken());
						break;
					case 5: 
						kvResult.addPair("TeacherFirst", strTok.nextToken());
						break;
					case 6: 
						kvResult.addPair("TeacherLast", strTok.nextToken());
						break;
					default:
						strTok.nextToken();
						break;
				
				}
			}

			pr.println (head);

			msgReceived = br.readLine();

			//msgReceived = br.readLine(); /* MODIFIED - Something caused this to hang, had to comment it out*/

			strTok = new StringTokenizer(msgReceived, delim);
			countTokens = strTok.countTokens();

			for (int i = 0; i < countTokens; i++)
			{
				if(i > 1)
				{
					if(i%2 == 0)
						kvResult.addPair("FirstName" + (i-2)/2, strTok.nextToken());	
					else
						kvResult.addPair("LastName" + (i-3)/2, strTok.nextToken());	
				}
				else
					strTok.nextToken();
			}

			s.close();
			s = null;
			mEncoder.sendMsg(kvResult, client.getOutputStream());
                        mEncoder.sendMsg(kvResult, component.getOutputStream()); /* MODIFIED */
		}
		catch (Exception ex)
		{
		}
	}

	public void sendCourseSelection(String first, String last, String courseSelected)
	{
		if(state == success)
		{
			try
			{
				s = new Socket (vcAddress, 1189);
				inStream = s.getInputStream();
				outStream = s.getOutputStream();             
				
				pr = new PrintWriter (outStream, true);
				br = new BufferedReader(new InputStreamReader(inStream));    
				
				/* thread to receive messages*/
				Thread t = new Thread( new Runnable()
				{
					public void run()
					{
						try
						{
							MsgEncoder mEncoder= new MsgEncoder();

							while( true )
							{
								KeyValueList kvResult = new KeyValueList();

								msgReceived = br.readLine();
								strTok = new StringTokenizer(msgReceived, delim);
								int countTokens = strTok.countTokens();
								
								String fname, lname;
								for (int i =0; i < countTokens; i++)
								{
									switch (i)
									{
										case 3:
											int cmd = Integer.parseInt(strTok.nextToken());
											switch (cmd)
											{
												case 9:
													kvResult = classRoster(msgReceived);
													break;
												case 2:
													kvResult = broadcastMsg(msgReceived);
													break;
												case 13: 
													kvResult = privateMsg(msgReceived);
													break;
												case 4:
													kvResult = stateUpdate(msgReceived);
													break;
												case 7:
													kvResult = stateUpdate(msgReceived);
													break;
												case 1:
													kvResult = urlPush(msgReceived);
													break;
												case 3:
													kvResult = boardDrawing(msgReceived);
													break;
												default:
													kvResult.addPair("Description", "Unknown MsgID");
													kvResult.addPair("Msg", msgReceived);
													break;
											
											}
											countTokens = 0;
											break;
										default:
											strTok.nextToken();
											break;
									}
								}

								mEncoder.sendMsg(kvResult, client.getOutputStream());
								mEncoder.sendMsg(kvResult, component.getOutputStream());

							}
						}
						catch ( Exception e )
						{ System.out.println("Exception!"); }
					}
				});
				t.setDaemon( true );
				t.start();
				/* end of receiving thread*/

				int code = 1;
				if (first.equalsIgnoreCase("teacher"))
					code = 2;

				msgSent = head + delim + code + delim + first + delim + last + delim + courseSelected;
				pr.println (msgSent);

			}
			catch(Exception e){
                            System.out.println("Exception in sendCourseSelection " + e.getMessage());
                        }
					
		}
	}

	public void sendBroadcast(String msgColor, String isAudio, String msg)
	{
		if(state == success && s != null )
		{
			try
			{		
				int cmd = 2;
				msgSent = head + delim + cmd + delim + msgColor + delim + isAudio + delim + msg;
				pr.println (msgSent);
			}
			catch(Exception e){}
					
		}
	}

	public void updateStatus(String status, String isTeacher)
	{
		if(state == success && s != null )
		{
			try
			{				
				int imgMode = 0;
				int cmd = 4;
				if(isTeacher.equalsIgnoreCase("true"))
					cmd = 7;
				msgSent = head + delim + cmd + delim + imgMode + delim + status;
				pr.println (msgSent);
			}
			catch(Exception e){}
					
		}
	}

	public void pushURL(String mainURL, String audioURL, String videoURL, String forTeacherOnly)
	{
		if(state == success && s != null )
		{
			try
			{				
				int cmd = 1;
				msgSent = head + delim + cmd + delim + mainURL + delim + audioURL + delim + 
							videoURL + delim + forTeacherOnly;
				pr.println (msgSent);
			}
			catch(Exception e){}
					
		}
	}
    
	public void sendMsgPrivate(String toFirstName, String toLastName, String msgColor, String isAudio, String msg)
	{
		if(state == success && s != null )
		{
			try
			{				
				int cmd = 13;
				msgSent = head + delim + cmd + delim + toFirstName + " " + toLastName + delim + 
							msgColor + delim + isAudio + delim + msg;
				pr.println (msgSent);
			}
			catch(Exception e){}
					
		}
	}

	public void sendBoardDrawing(KeyValueList kvList)
	{
		if(state == success && s != null )
		{
			try
			{	
				int cmd = 3;
				msgSent = head + delim + cmd;
				int numOfShapes = Integer.parseInt(kvList.getValue("ShapeNum"));
				for (int i= 0; i <numOfShapes; i++)
				{
					String shapeType = kvList.getValue("ShapeType"+i);
					if(shapeType.equals("4"))
					{
						String xPos = kvList.getValue("PosX"+i);
						String yPos = kvList.getValue("PosY"+i);
						String textStr = kvList.getValue("TextStr"+i);

						msgSent += delim + shapeType + delim + xPos + delim + yPos + delim + textStr;
					}
					else
					{
						String leftTopX = kvList.getValue("LeftTopX"+i);
						String leftTopY = kvList.getValue("LeftTopY"+i);
						String rightBottomX = kvList.getValue("RightBottomX"+i);
						String rightBottomY = kvList.getValue("RightBottomY"+i);
						String lineStyle = kvList.getValue("LineStyle"+i);
						String lineColor = kvList.getValue("LineColor"+i);
						String lineWidth = kvList.getValue("LineWidth"+i);
						String brushStyle = kvList.getValue("BrushStyle"+i);
						String brushColor = kvList.getValue("BrushColor"+i);

						msgSent += delim + shapeType + delim + leftTopX + delim + leftTopY + delim +
								rightBottomX + delim + rightBottomY + delim + lineStyle + delim + 
								lineColor + delim + lineWidth + delim + brushStyle + delim + brushColor;
					}
				}
				
				pr.println (msgSent);
			}
			catch(Exception e){}
					
		}
	}

    /* function in interface ComponentBase */
    
    synchronized public void processMsg(KeyValueList kvList) throws Exception
	{
		int MsgID = Integer.parseInt(kvList.getValue("MsgID"));
		switch (MsgID)
		{
			case 0:
				doAuthentication(kvList.getValue("FirstName"), 
							kvList.getValue("LastName"), kvList.getValue("passwd"), kvList.getValue("IsTeacher"));
				break;
			case 3:
				selectCourseNS(kvList.getValue("CourseSelected"));
				sendCourseSelection(kvList.getValue("FirstName"), kvList.getValue("LastName"), 
							kvList.getValue("CourseSelected"));
				break;
			case 5:
				updateStatus(kvList.getValue("State"), kvList.getValue("IsTeacher"));
				break;
			case 7:
				pushURL(kvList.getValue("MainURL"), kvList.getValue("AudioURL"), kvList.getValue("VideoURL"),
							kvList.getValue("ForTeacherOnly"));
				break;
			case 8:
				String tmp = kvList.getValue("ToFirstName");
				if (tmp.trim().equals(""))
					sendBroadcast(kvList.getValue("MsgColor"), kvList.getValue("IsAudio"), kvList.getValue("Msg"));
				else
					sendMsgPrivate(kvList.getValue("ToFirstName"), kvList.getValue("ToLastName"), 
							kvList.getValue("MsgColor"), kvList.getValue("IsAudio"), kvList.getValue("Msg"));
				break;
			case 9:
				sendBoardDrawing(kvList);
				break;
			default:
				sendToComponent(kvList);
				break;

		}
    }

	public KeyValueList classRoster(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();

		kvResult.addPair("MsgID", "4");
		kvResult.addPair("Description", "Class Roster");
		for (int i = 0; i < countTokens; i++)
		{
			if(i > 4)
			{
				if(i%3 == 0)
					kvResult.addPair("LastName" + (i-6)/3, strTok.nextToken());	
				else if(i%3 == 1)
					kvResult.addPair("State" + (i-7)/3, strTok.nextToken());	
				else
					kvResult.addPair("FirstName" + (i-5)/3, strTok.nextToken());
			}
			else if(i == 4)
				kvResult.addPair("StudentCount", strTok.nextToken());
			else if(i == 3)
			{
				if(!(strTok.nextToken()).equals("9"))
					break;
			}
			else
				strTok.nextToken();
		}

		return kvResult;
	}

	public KeyValueList broadcastMsg(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();
		
		kvResult.addPair("MsgID", "8");
		kvResult.addPair("Description", "Instant Message");
		for (int i = 0; i < countTokens; i++)
		{
			switch (i)
			{
				case 1:
					kvResult.addPair("FromFirstName", strTok.nextToken());
					break;
				case 2:
					kvResult.addPair("FromLastName", strTok.nextToken());
					break;
				case 4:
					kvResult.addPair("MsgColor", strTok.nextToken());
					break;
				case 5:
					kvResult.addPair("IsAudio", strTok.nextToken());
					break;
				case 6:
					kvResult.addPair("Msg", strTok.nextToken());
					break;
				default:
					strTok.nextToken();
					break;
			}
		}

		return kvResult;
	}

	public KeyValueList stateUpdate(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();

		kvResult.addPair("MsgID", "5");
		kvResult.addPair("Description", "Changing State");
		for (int i = 0; i < countTokens; i++)
		{
			switch (i)
			{
				case 1:
					kvResult.addPair("FirstName", strTok.nextToken());
					break;
				case 2:
					kvResult.addPair("LastName", strTok.nextToken());
					break;
				case 4:
					kvResult.addPair("ImageMode", strTok.nextToken());
					break;
				case 5:
					kvResult.addPair("State", strTok.nextToken());
					break;
				default:
					strTok.nextToken();
					break;
			}
		}
		return kvResult;
	}

	public KeyValueList privateMsg(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();

		kvResult.addPair("MsgID", "8");
		kvResult.addPair("Description", "Instant Message");
		for (int i = 0; i < countTokens; i++)
		{
			switch (i)
			{
				case 1:
					kvResult.addPair("FromFirstName", strTok.nextToken());
					break;
				case 2:
					kvResult.addPair("FromLastName", strTok.nextToken());
					break;
				case 4:
					String temp = strTok.nextToken();
					StringTokenizer st = new StringTokenizer(temp);
					kvResult.addPair("ToFirstName", st.nextToken());
					kvResult.addPair("ToLastName", st.nextToken());
					break;
				case 5:
					kvResult.addPair("MsgColor", strTok.nextToken());
					break;
				case 6:
					kvResult.addPair("IsAudio", strTok.nextToken());
					break;
				case 7:
					kvResult.addPair("Msg", strTok.nextToken());
					break;
				default:
					strTok.nextToken();
					break;
			}
		}
		return kvResult;
	}

	public KeyValueList urlPush(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();

		kvResult.addPair("MsgID", "7");
		kvResult.addPair("Description", "URLs Pushed");
		for (int i = 0; i < countTokens; i++)
		{
			switch (i)
			{
				case 1:
					kvResult.addPair("FirstName", strTok.nextToken());
					break;
				case 2:
					kvResult.addPair("LastName", strTok.nextToken());
					break;
				case 4:
					kvResult.addPair("MainURL", strTok.nextToken());
					break;
				case 5:
					kvResult.addPair("AudioURL", strTok.nextToken());
					break;
				case 6:
					kvResult.addPair("VideoURL", strTok.nextToken());
					break;
				case 7:
					kvResult.addPair("ForTeacherOnly", strTok.nextToken());
					break;
				default:
					strTok.nextToken();
					break;
			}
		}
		return kvResult;
	}

	public KeyValueList boardDrawing(String msgReceived)
	{
		KeyValueList kvResult = new KeyValueList();
		strTok = new StringTokenizer(msgReceived, delim);
		int countTokens = strTok.countTokens();

		kvResult.addPair("MsgID", "9");
		kvResult.addPair("Description", "Whiteboard Drawing");
		kvResult.addPair("ShapeNum", "0");
		int shapeNum = 0;
		for (int i = 0; i < countTokens; )
		{
			if(i < 4)
			{
				strTok.nextToken();
				i++;
			}
			else
			{
				String curr = strTok.nextToken();
				i++;
				if(curr.equals("4"))
				{
					kvResult.addPair("ShapeType"+shapeNum, curr);
					kvResult.addPair("PosX"+shapeNum, strTok.nextToken());
					kvResult.addPair("PosY"+shapeNum, strTok.nextToken());
					kvResult.addPair("TextStr"+shapeNum, strTok.nextToken());
					i += 3;
				}
				else
				{
					kvResult.addPair("ShapeType"+shapeNum, curr);
					kvResult.addPair("LeftTopX"+shapeNum, strTok.nextToken());
					kvResult.addPair("LeftTopY"+shapeNum, strTok.nextToken());
					kvResult.addPair("RightBottomX"+shapeNum, strTok.nextToken());
					kvResult.addPair("RightBottomY"+shapeNum, strTok.nextToken());
					kvResult.addPair("LineStyle"+shapeNum, strTok.nextToken());
					kvResult.addPair("LineColor"+shapeNum, strTok.nextToken());
					kvResult.addPair("LineWidth"+shapeNum, strTok.nextToken());
					kvResult.addPair("BrushStyle"+shapeNum, strTok.nextToken());
					kvResult.addPair("BrushColor"+shapeNum, strTok.nextToken());
					i += 9;
				}

				shapeNum++;
			}
		}
		int index = kvResult.lookupKey("ShapeNum");
		kvResult.setValue(index, "" + shapeNum);

		return kvResult;
	}

	public void sendToComponent(KeyValueList kvList)
	{
		try
		{
			MsgEncoder mEncoder = new MsgEncoder();
			if(component != null)
				mEncoder.sendMsg(kvList, component.getOutputStream());
		}
		catch(Exception ex)
		{ System.out.println("Problem sending to component!");}
	}
}
