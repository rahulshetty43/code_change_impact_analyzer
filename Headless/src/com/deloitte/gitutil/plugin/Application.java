package com.deloitte.gitutil.plugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import com.deloitte.gitutil.plugin.model.CallerCallee;
import com.deloitte.gitutil.plugin.model.ChangeSet;
import com.deloitte.gitutil.plugin.util.ConfigProvider;





/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	Map<String,String> callerscallee  = new HashMap<String,String>();
	@Override
	public Object start(IApplicationContext context) throws Exception {
		System.out.println("@Start");
	
		String[] str = (String[]) context.getArguments().get("application.args");	
		System.out.println(str[0]);
		System.out.println(str[1]);
		
		ConfigProvider.init(str[0], str[1]);
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject[] projects = root.getProjects();
		
			for (IProject project : projects) {
			System.out.println("Project Name"+project.getName());
			}
			
			
			try{
				ArrayList<ChangeSet> input = getChangeSetInput();
				if(input != null){
					getAllCallersBasedOnMethod(input);
				}
				System.out.println("Result :");
				System.out.println();
				ArrayList<CallerCallee> callerCallee = new ArrayList<>();
				for(Map.Entry<String,String > CallerCallee : callerscallee.entrySet()){
					String Caller=CallerCallee.getValue();
					String Callee = CallerCallee.getKey();
					System.out.println(Callee + "--->" +Caller);
					System.out.println();

					callerCallee.add(new CallerCallee(Callee,Caller));
				}
				write(callerCallee);
				sendMail(callerscallee);
				System.out.println();
				Application a = new Application();			
					System.exit(0);			
			}catch(Exception e){
				System.out.println("Exception ocurred");
				e.printStackTrace();
			}

		
			
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
	
	void sendMail(Map<String, String> callerscallee) throws AddressException, MessagingException{
		System.out.println("Configuring SMTP");
		
		Properties props = new Properties();

//		final String username = "sprakashece@gmail.com";
//		final String password = "1100140889";	
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.port", "465");
//		props.put("mail.smtp.starttls.enable", "true");
		
		
		props.put("mail.smtp.host", ConfigProvider.mailConfig.getSmtp_host());
		props.put("mail.smtp.auth",ConfigProvider.mailConfig.getSmtp_auth());
		props.put("mail.smtp.port", ConfigProvider.mailConfig.getSmtp_port());
		
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.port", ConfigProvider.mailConfig.getSmtp_socketFactory_port());
	
		props.put("mail.smtp.starttls.enable", ConfigProvider.mailConfig.getSmtp_starttls_enable());

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(ConfigProvider.mailConfig.getUsername(), ConfigProvider.mailConfig.getPassword());
			}
		});

		session.setDebug(true);

		Message message = new MimeMessage(session);

		message.setFrom(new InternetAddress(ConfigProvider.mailConfig.getUsername()));
//		message.setRecipients(Message.RecipientType.TO,
//				InternetAddress.parse("ashiev@deloitte.com")); 
		
		message.addRecipients(Message.RecipientType.CC, 
                InternetAddress.parse(ConfigProvider.mailConfig.getMail_to()));
//		message.addRecipients(Message.RecipientType.CC, 
//                InternetAddress.parse("ashiev@deloitte.com,rahulc8@deloitte.com"));

		message.setSubject(ConfigProvider.mailConfig.getSubject());
		String messageBody = ConfigProvider.mailConfig.getBody();
		String changeSet = "";
		for(Map.Entry<String,String > CallerCallee : callerscallee.entrySet()){
			String key=CallerCallee.getKey();
			String inputPackageName = key.substring(0, key.indexOf("*"));
			String inputClassName = key.substring(key.indexOf("*")+1,key.indexOf("-"));
			String inputMethodName = key.substring(key.indexOf("-"));
			
			String value = CallerCallee.getValue();
			String rootPackageName = value.substring(0, value.indexOf("*"));
			String rootClassName = value.substring(value.indexOf("*")+1);
			
//			String rootClassName = CallerCallee.getValue().substring(0, CallerCallee.getValue().indexOf("*"));
//			String methodName = CallerCallee.getValue().substring( CallerCallee.getValue().indexOf("-")+1);
			
			changeSet=changeSet+"<tr>";
			changeSet=changeSet+"<td>"+rootPackageName+"</td>"+"<td>"+rootClassName+"</td>"+"<td>"+inputPackageName+"."+inputClassName+inputMethodName+"</td>\n";
			changeSet=changeSet+"</tr>";
		}
		
		messageBody += changeSet+"</table>";
		messageBody += ConfigProvider.mailConfig.getRegards();
		message.setContent(messageBody,"text/html" );  

		Transport.send(message);
	}


	
	public static void write(ArrayList<CallerCallee> callerCallee) {
		try
		{
			PrintWriter pw = new PrintWriter("C:\\Users\\prsekar\\Desktop\\cf\\Disney\\storage\\callerCallee.csv");
			pw.flush();			
			for(CallerCallee cs : callerCallee) {
				System.out.println(cs);
				pw.append(cs.toString());
			}
			            
			pw.close();

		}catch(IOException i)
		{
			i.printStackTrace();
		}

	}
	public ArrayList getChangeSetInput(){

		try {

			BufferedReader reader = new BufferedReader(new FileReader(
					"C:\\Users\\prsekar\\Desktop\\cf\\Disney\\storage\\changeSet.csv"));
			String line = null;
			ArrayList<ChangeSet>  changeSetList = new ArrayList<>();
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				ChangeSet cs = new ChangeSet();
				String[] csString = line.split(",");
				cs.setProjectName(csString[0]);
				cs.setPackageName(csString[1]);
				cs.setClassName(csString[2]);
				cs.setMethodName(csString[3]);
				changeSetList.add(cs);
			}

			return changeSetList;
		} catch ( IOException e) {
			e.printStackTrace();
		}

		return null;

	}
	void getAllCallersBasedOnMethod(ArrayList<ChangeSet> ChangeSet){
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			IProject[] projects = root.getProjects();

			for(Object in : ChangeSet){
				for (IProject project : projects) {
					IJavaProject javaProject = null;
					if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
						javaProject = JavaCore.create(project);
						IPackageFragment[] packages = javaProject.getPackageFragments();
						for (IPackageFragment mypackage : packages) {
							if (mypackage.getElementName().equals(((ChangeSet)in).getPackageName()) && mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
								for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
									if(unit.getElementName().equals(((ChangeSet)in).getClassName())){
										IType[] allTypes = unit.getAllTypes();
										for (IType type : allTypes) {
											IMethod[] methods = type.getMethods();
											for (IMethod method : methods) {

												String methodName = ((ChangeSet)in).getMethodName().substring(0, ((ChangeSet)in).getMethodName().indexOf("("));
												if(((method.getElementName().equalsIgnoreCase(methodName)))){
													String signature=resolveMethodSignature(method);
													String params=(String) ((ChangeSet)in).getMethodName().subSequence((((ChangeSet)in).getMethodName().indexOf("(")), (((ChangeSet)in).getMethodName().indexOf(")")+1));
													if(params.equals(signature)){
														HashSet<IMethod> a=getCallersOf(method);
													}
												}
											}

										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("null")
	public static String resolveMethodSignature(IMethod method) throws JavaModelException {
		String signature = method.getSignature();
		String[] parameterTypes = Signature.getParameterTypes(signature);
		int length = parameterTypes.length;
		String[] resolvedParameterTypes = new String[length];
		for (int i = 0; i < length; i++) {
			resolvedParameterTypes[i] = resolveTypeSignature(method, parameterTypes[i]);
			if (resolvedParameterTypes[i] == null) {
				return null;
			}
		}
		String finalSignature="(";
		for(int i=0;i<resolvedParameterTypes.length;i++){
			if(i==resolvedParameterTypes.length-1){
				finalSignature=finalSignature+resolvedParameterTypes[i];
			}
			else
			{
				finalSignature=finalSignature+resolvedParameterTypes[i]+",";
			}
		}
		finalSignature=finalSignature+")";
		return finalSignature;
	}
	private static String resolveTypeSignature(IMethod method, String typeSignature) throws JavaModelException {
		int count = Signature.getArrayCount(typeSignature);
		String elementTypeSignature = Signature.getElementType(typeSignature);
		String elementTypeName = Signature.toString(elementTypeSignature);
		return elementTypeName;

	}
	

	/*HashSet<IMethod> getCallersOf(IMethod m) throws JavaModelException {
		final IMethod met= m;
		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		IMember[] members = {m};

		MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		HashSet<IMethod> callers = new HashSet<IMethod>();
		for (MethodWrapper mw : methodWrappers) {
			MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(mw2);
			callers.addAll(temp);  
			if(!callers.isEmpty()){
				for(IMethod subCaller : callers){
					//					System.out.println("Method " + m.getElementName() + " called by :" );
					//					System.out.println();
					//					System.out.println( "-----" + subCaller.getParent().getParent().getParent().getElementName());
					//					System.out.println("---" + subCaller.getParent().getElementName());
					//					System.out.println("-" + subCaller.getElementName() );
					//					System.out.println();
					HashSet<IMethod> parentCaller = getCallersOf(subCaller);
					if((parentCaller==null || parentCaller.size()==0) )
					{
						String signature=resolveMethodSignature(m);
						//						System.out.println("Parent method of -"+m.getElementName() +signature+" are - "+subCaller.getParent().getParent().getParent().getElementName()+"."+subCaller.getParent().getElementName()+"."+subCaller.getElementName());
						callerscallee.put(subCaller.getParent().getElementName()+".java", subCaller.getParent().getParent().getParent().getElementName());
					}
				}
			}
			else{
				System.out.println("");
				callerscallee.put(m.getParent().getElementName()+".java", m.getParent().getParent().getParent().getElementName());
			}
		}
		return callers;
	}*/
	
	HashSet<IMethod> getCallersOf(IMethod m) throws JavaModelException {
		final IMethod met= m;
		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		IMember[] members = {m};

		MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		HashSet<IMethod> callers = new HashSet<IMethod>();
		for (MethodWrapper mw : methodWrappers) {
			MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(mw2);
			callers.addAll(temp);  
			if(!callers.isEmpty()){
				for(IMethod subCaller : callers){
					//					System.out.println("Method " + m.getElementName() + " called by :" );
					//					System.out.println();
					//					System.out.println( "-----" + subCaller.getParent().getParent().getParent().getElementName());
					//					System.out.println("---" + subCaller.getParent().getElementName());
					//					System.out.println("-" + subCaller.getElementName() );
					//					System.out.println();
					HashSet<IMethod> parentCaller = getCallersOf(subCaller);
					if((parentCaller==null || parentCaller.size()==0) )
					{
						String signature=resolveMethodSignature(m);
						//						System.out.println("Parent method of -"+m.getElementName() +signature+" are - "+subCaller.getParent().getParent().getParent().getElementName()+"."+subCaller.getParent().getElementName()+"."+subCaller.getElementName());
//						callerscallee.put(subCaller.getParent().getParent().getParent().getElementName(),subCaller.getParent().getElementName()+".java" +"-"+m.getParent().getParent().getElementName()+"-"+m.getParent().getElementName()+"."+m.getElementName());
						callerscallee.put(m.getParent().getParent().getParent().getElementName()+"*"+m.getParent().getParent().getElementName()+"-"+m.getElementName(),
								subCaller.getParent().getParent().getElementName()+"*"+subCaller.getParent().getElementName()+"-"+subCaller.getElementName());
					}
				}
			}
			else{
				System.out.println("");
				//input package*class-method, root package*classname
//				callerscallee.put(m.getParent().getParent().getParent().getElementName(),m.getParent().getElementName()+".java" +"-"+m.getParent().getParent().getElementName()+"."+m.getParent().getElementName()+"."+m.getElementName());
				callerscallee.put(m.getParent().getParent().getParent().getElementName()+"*"+m.getParent().getParent().getElementName()+"-"+m.getElementName(),
						m.getParent().getParent().getParent().getElementName()+"*"+m.getParent().getParent().getElementName());
			}
		}
		return callers;
	}


	HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> c = new HashSet<IMethod>(); 
		for (MethodWrapper m : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(m);
			if (im != null) {
				c.add(im);
			}
		}
		return c;
	}

	IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
		try {
			IMember im = m.getMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod)m.getMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}


