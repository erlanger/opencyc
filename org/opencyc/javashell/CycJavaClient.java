package org.opencyc.javashell;
/**
 * This class implements a CycJavaClient.
 *
 * @version $Id$
 * @author Stephen L. Reed
 *
 * <p>Copyright 2001 Cycorp, Inc., license is open source GNU LGPL.
 * <p><a href="http://www.opencyc.org/license.txt">the license</a>
 * <p><a href="http://www.opencyc.org">www.opencyc.org</a>
 * <p><a href="http://www.sourceforge.net/projects/opencyc">OpenCyc at SourceForge</a>
 * <p>
 * THIS SOFTWARE AND KNOWLEDGE BASE CONTENT ARE PROVIDED ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OPENCYC
 * ORGANIZATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE AND KNOWLEDGE
 * BASE CONTENT, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.opencyc.api.*;
import org.opencyc.util.*;
import org.opencyc.cycobject.*;
import org.opencyc.cyclobject.*;
import org.opencyc.cycagent.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;

public class CycJavaClient extends Thread {
    private CycJavaShell jshell = null;
    private Socket clientSocket = null;
    private Hashtable clientPeers = null;
    private InputStream in = null;
    private OutputStream out = null;
    private CycAccess cycAccess = null;
    private static CycSymbol QUIT_COMMAND = new CycSymbol("API-QUIT");
    
    public CycJavaClient(Socket client,CycJavaShell jshell) throws IOException {
	this.jshell = jshell;
	clientSocket = client; 
	in = client.getInputStream();
	out = client.getOutputStream();
	cycAccess = new CycAccess(client.getInetAddress().getHostAddress(),
				  CycConnection.DEFAULT_BASE_PORT,CycConnection.DEFAULT_COMMUNICATION_MODE,CycAccess.DEFAULT_CONNECTION);                   
	jshell.ensureClientSupportsShell(cycAccess);

    }

    public void run() {
	PrintStream outstream = new PrintStream(out);
	CycListParser cyclp = CycListParser(cycAccess); 
	while( !this.interrupted() && in!=null && out!=null ) {
	    StreamTokenizer st = new StreamTokenizer(in);
	    st.commentChar( ';' ); st.ordinaryChar( '(' ); st.ordinaryChar( ')' ); st.ordinaryChar( '\'' ); st.ordinaryChar( '`' ); st.ordinaryChar( '.' );
	    st.wordChars( '=', '=' ); st.wordChars( '+', '+' ); st.wordChars( '-', '-' ); st.wordChars( '_', '_' ); st.wordChars( '<', '<' ); st.wordChars( '>', '>' );
	    st.wordChars( '*', '*' ); st.wordChars( '/', '/' ); st.wordChars( '.', '.' ); st.wordChars( '#', '#' ); st.wordChars( ':', ':' ); st.wordChars( '!', '!' );
	    st.wordChars( '$', '$' ); st.wordChars( '?', '?' ); st.wordChars( '%', '%' ); st.wordChars( '&', '&' );
	    try {
	    CycList todo = cyclp.read(st);
	    if(todo.first().equals(QUIT_COMMAND)) return;
		CycList result = jshell.invoke(cycAccess,todo);        
		outstream.println("200 "+result.stringApiValue());
	    } catch (Exception e){
		outstream.println("500 \""+e+"\"");
	    }
	}
    }
}

