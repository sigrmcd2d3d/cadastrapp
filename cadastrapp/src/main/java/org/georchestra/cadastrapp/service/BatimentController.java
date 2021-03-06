package org.georchestra.cadastrapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.georchestra.cadastrapp.helper.BatimentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;


public class BatimentController extends CadController {

	final static Logger logger = LoggerFactory.getLogger(BatimentController.class);
	
	@Autowired
	BatimentHelper batimentHelper;
	
	@GET
	@Path("/getBatiments")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * getBuildingsDetails
	 *  Returns information about batiment dnubat on given parcell 
	 *  
	 * @param headers http headers used 
	 * @param parcelle parcelle id
	 * @param dnubat batiment number 
	 * 
	 * @return JSON list 
	 */
	public List<Map<String, Object>> getBuildingsDetails(@Context HttpHeaders headers, 
			@QueryParam("parcelle") String parcelle,
			@QueryParam("dnubat") String dnubat){
		
		List<Map<String, Object>> batiments = new ArrayList<Map<String, Object>>();
		if (getUserCNILLevel(headers) == 0) {
			logger.info("User does not have enough rights to see information about batiment");
		}
		else if(parcelle != null && !parcelle.isEmpty()
				&& dnubat != null && !dnubat.isEmpty())
		{
			logger.debug("infoOngletBatiment - parcelle : " + parcelle + " for dnubat : " + dnubat);
			
			List<String> queryParams = new ArrayList<String>();
			queryParams.add(parcelle);
			queryParams.add(dnubat);
			
			StringBuilder queryBuilder = new StringBuilder();
			
			// CNIL Niveau 2
			queryBuilder.append("select distinct hab.annee, pb.jannat, pb.invar, pb.descr, pb.dniv, pb.dpor, pb.revcad, hab.ccoaff_lib, ");
			queryBuilder.append("prop.comptecommunal, prop.dnupro, prop.app_nom_usage, prop.app_nom_naissance ");
			queryBuilder.append("from ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".proprietebatie pb, ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".proprietaire prop, ");
			queryBuilder.append(databaseSchema);
			queryBuilder.append(".deschabitation hab ");
			queryBuilder.append(" where pb.parcelle = ? ");
			queryBuilder.append(" and pb.comptecommunal = prop.comptecommunal ");
			queryBuilder.append(" and pb.dnubat = ? and hab.invar = pb.invar ;");

			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			batiments = jdbcTemplate.queryForList(queryBuilder.toString(), queryParams.toArray());	
		}
		else{
			logger.info(" Missing input parameter ");
		}
		return batiments;
	}

	
	@GET
	@Path("/getBatimentsByParcelle")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 *  Returns all building from given plot 
	 *  
	 * @param headers http headers used only available for CNIL 2
	 * @param parcelle on unique plot id
	 * 
	 * @return JSON list compose with all dnubat from this plot, list is empy if no data, or if user doesn't have rights
	 */
	public List<Map<String, Object>> getBuildingsByParcelle(@Context HttpHeaders headers, 
			@QueryParam("parcelle") String parcelle){
		
		List<Map<String, Object>> batiments = new ArrayList<Map<String, Object>>();
		if (getUserCNILLevel(headers) == 0) {
			logger.info("User does not have enough rights to see information about buildings");
		}
		else if(parcelle != null && !parcelle.isEmpty())
		{
			batiments = batimentHelper.getBuildings(parcelle, headers);
		}
		else{
			logger.info(" Missing input parameter ");
		}
		return batiments;
	}

}
