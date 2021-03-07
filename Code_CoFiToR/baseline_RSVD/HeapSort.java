import java.util.Collections;
import java.util.List;
import java.util.Map;

//小根堆, 没有保证"左小右大", 只保证每个tree或subtree的dad节点的值比son节点的值要大
class HeapSort
{
	// 整理节点
	private static void minHeapify(List<Map.Entry<Integer, Float>> list, int start, int end)
	{
		int dad = start;
		int son = dad*2 + 1;
		
		while(son<=end)
		{
			if(son+1<=end && list.get(son).getValue() > list.get(son+1).getValue())
			{
				// 寻找具有最小值的son
				son++;
			}
			
			if(list.get(dad).getValue() < list.get(son).getValue())
			{
				// dad的值已经是最小的了
				return;
			}
			else
			{
				// dad的值不是最小时的情况
				Collections.swap(list, dad, son);
				// top-down的模式
				dad = son;
				son = dad*2 + 1;
			}
		}
	}
	
	// min heap sort
	static List<Map.Entry<Integer, Float>> heapSort(List<Map.Entry<Integer, Float>> list, int k)
	{
		int end = k-1;
		
		// the first k elements (bottom-up的模式)
		for(int i=k/2-1; i>=0; i--)
		{
			minHeapify(list, i, end);
		}
				
		// the remaining elements
		for(int i=k; i<list.size(); i++)
		{
			if(list.get(i).getValue() > list.get(0).getValue())
			{
				Collections.swap(list, i, 0);
				minHeapify(list, 0, end);
			}
		}
		
		// put the largest in the beginning
		for(int i=k-1; i>0; --i)
		{
			minHeapify(list, 0, i);
			Collections.swap(list, 0, i);
		}
		
		return list;
	}

}