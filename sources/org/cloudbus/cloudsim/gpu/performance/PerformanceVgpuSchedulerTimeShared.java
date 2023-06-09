package org.cloudbus.cloudsim.gpu.performance;

import org.cloudbus.cloudsim.gpu.Pgpu;
import org.cloudbus.cloudsim.gpu.Vgpu;
import org.cloudbus.cloudsim.gpu.VgpuScheduler;
import org.cloudbus.cloudsim.gpu.VgpuSchedulerTimeShared;
import org.cloudbus.cloudsim.gpu.performance.models.PerformanceModel;
import org.cloudbus.cloudsim.gpu.selection.PgpuSelectionPolicy;

import java.util.List;

/**
 * {@link PerformanceVgpuSchedulerTimeShared} extends
 * {@link VgpuSchedulerTimeShared
 * VgpuSchedulerTimeShared} to add support for
 * {@link PerformanceModel
 * PerformanceModels}.
 * 
 * @author Ahmad Siavashi
 * 
 */
public class PerformanceVgpuSchedulerTimeShared extends VgpuSchedulerTimeShared implements PerformanceScheduler<Vgpu> {

	/** The performance model */
	private PerformanceModel<VgpuScheduler, Vgpu> performanceModel;

	/**
	 * @see VgpuSchedulerTimeShared#VgpuSchedulerTimeShared(int,
	 *      List, PgpuSelectionPolicy) VgpuSchedulerTimeShared(int, List,
	 *      PgpuSelectionPolicy)
	 * @param performanceModel
	 *            the performance model
	 */
	public PerformanceVgpuSchedulerTimeShared(String videoCardType, List<Pgpu> pgpuList,
			PgpuSelectionPolicy pgpuSelectionPolicy, PerformanceModel<VgpuScheduler, Vgpu> performanceModel) {
		super(videoCardType, pgpuList, pgpuSelectionPolicy);
		this.performanceModel = performanceModel;
	}

	@Override
	public List<Double> getAvailableMips(Vgpu vgpu, List<Vgpu> vgpuList) {
		return this.performanceModel.getAvailableMips(this, vgpu, vgpuList);
	}
}
