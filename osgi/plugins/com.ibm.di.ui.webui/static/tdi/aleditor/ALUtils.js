define([
	"tdi/tdiconfig"
	], function(tdiconfig) {
		var utils = {};
		
		utils.getTooltip = function(config) {
			if(config.isBranch())
				return utils.getBranchTooltip(config);
			else if(config.isLoop())
				return utils.getLoopTooltip(config);
		}
		
		utils.getBranchTooltip = function(config) {
			return "<b>" + config.getName() + "</b><hr></hr>branch config";
		}
		
		utils.getBranchTooltip = function(config) {
			return "<b>" + config.getName() + "</b><hr></hr>loop config";
		}
		
		return utils;
	}
)