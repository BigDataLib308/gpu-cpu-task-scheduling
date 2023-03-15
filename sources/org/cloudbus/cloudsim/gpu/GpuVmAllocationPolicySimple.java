package org.cloudbus.cloudsim.gpu;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

/**
 * {@link GpuVmAllocationPolicySimple} extends {@link GpuVmAllocationPolicy} and
 * implements first-fit algorithm for VM placement.
 * 
 * @author Ahmad Siavashi
 *
 */
public class GpuVmAllocationPolicySimple extends GpuVmAllocationPolicy {

	/**
	 * @param list
	 */
	public GpuVmAllocationPolicySimple(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		//将虚拟机分配到主机中
		if (!getVmTable().containsKey(vm.getUid())) {
			GpuVm gpuVm = (GpuVm) vm;

//			Host host2 = getHostList().get(0);
//			double RLC_max=0;
//			for (Host host : getHostList()){
//				double LC = (host.getTotalStorage() - host.getStorage()) * 100/host.getTotalStorage();
//				double RLC = 100 - LC;
//				if(RLC>RLC_max){
//					host2 = host;
//					RLC_max = RLC;
//				}
//			}
//			System.out.println("Host #" + host2.getId() + " Storage: " + host2.getStorage()+" TotalStorage: "+host2.getTotalStorage()+" RLC_max: "+RLC_max);
			for (Host host : getHostList()) {
				boolean result = allocateHostForVm(vm, host);
				if (!result) {
					continue;
				} else if (!gpuVm.hasVgpu()) {
					return true;
				} else if (((GpuHost) host).isGpuEquipped() && allocateGpuForVgpu(gpuVm.getVgpu(), (GpuHost) host)) {
					return true;
				}
				deallocateHostForVm(gpuVm);

			}
		}
		return false;
	}
}
