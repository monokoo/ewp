package com.gz.tool.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.gz.tool.tls.TLS;

public class TLSFilter implements Filter {
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			System.out.println("TLSFilter-IN======");
			TLS tls = new TLS();
			tls.run(request, response, chain);
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}
	
}
