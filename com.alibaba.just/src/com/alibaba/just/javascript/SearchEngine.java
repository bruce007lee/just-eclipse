/**
 * 
 */
package com.alibaba.just.javascript;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.NodeVisitor;

/**
 * @author bruce.liz
 *
 */
public class SearchEngine {

	public static SearchResult findReference(final String content,final int offset,final int length){
		Parser  parser = new Parser();
		final AstRoot astRoot = parser.parse(content, null, 0);
		final SearchResult  rs = new SearchResult();

		astRoot.visit(new NodeVisitor(){

			public boolean visit(AstNode node) {
				if(node.getAbsolutePosition()==offset && node.getLength()==length){
					final int p = node.getAbsolutePosition();
					final int l = node.getLength();
					final String name = content.substring(p,p+l);

					astRoot.visit(new NodeVisitor(){

						private boolean found = false;

						public boolean visit(AstNode node0) {

							String xName = content.substring(node0.getAbsolutePosition(),node0.getAbsolutePosition()+node0.getLength());
							if(name.equals(xName) ){
								if(node0.getAbsolutePosition()!=p){
									if(rs.getPosition()<0 || found){
										rs.setPosition(node0.getAbsolutePosition());
										rs.setLength(node0.getLength());
									}
								}else{
									found = true;
								}

							}
							return true;
						}

					});

					return false;
				}
				return true;
			}

		});

		if(rs.getPosition()<0 && rs.getLength()<0){
			return null;
		}

		return rs;
	}
}
