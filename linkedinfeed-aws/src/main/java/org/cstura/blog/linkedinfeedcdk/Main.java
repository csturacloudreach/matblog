package org.cstura.blog.linkedinfeedcdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.CfnTable.TimeToLiveSpecificationProperty;
import software.amazon.awscdk.services.dynamodb.TableClass;
import software.amazon.awscdk.services.dynamodb.TableV2;

public class Main {

	public static void main(String[] args) {
		// TODO: use CDK to create a dynamodb collection where I will store my LinkedIn posts
		
		//now in theory the CDK will be able to pick up my AWS CLI configuration so no issue.
		App linkedInApp = new App();
		Stack appStack = Stack.Builder.create(linkedInApp,"LinkedInPostsBlog").env(Environment.builder().build()).build();
		
		//step 1: create a dynamodb collection on AWS to store my LinkedIn posts.
		TableV2 tblPosts = TableV2.Builder.create(appStack, "Table")
				.partitionKey(Attribute.builder().name("when").type(AttributeType.NUMBER).build())
				.tableName("linkedinposts")
				.tableClass(TableClass.STANDARD)
				.timeToLiveAttribute("expiresOn")
				.deletionProtection(false)
				.removalPolicy(RemovalPolicy.DESTROY)
				.build();			
		
		linkedInApp.synth();
	}

}
