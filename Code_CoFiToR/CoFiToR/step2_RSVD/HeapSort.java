import java.util.Collections;
import java.util.List;
import java.util.Map;

//С����, û�б�֤"��С�Ҵ�", ֻ��֤ÿ��tree��subtree��dad�ڵ��ֵ��son�ڵ��ֵҪ��
class HeapSort
{
	// ����ڵ�
	private static void minHeapify(List<Map.Entry<Integer, Float>> list, int start, int end)
	{
		int dad = start;
		int son = dad*2 + 1;
		
		while(son<=end)
		{
			if(son+1<=end && list.get(son).getValue() > list.get(son+1).getValue())
			{
				// Ѱ�Ҿ�����Сֵ��son
				son++;
			}
			
			if(list.get(dad).getValue() < list.get(son).getValue())
			{
				// dad��ֵ�Ѿ�����С����
				return;
			}
			else
			{
				// dad��ֵ������Сʱ�����
				Collections.swap(list, dad, son);
				// top-down��ģʽ
				dad = son;
				son = dad*2 + 1;
			}
		}
	}
	
	// min heap sort
	static List<Map.Entry<Integer, Float>> heapSort(List<Map.Entry<Integer, Float>> list, int k)
	{
		int end = k-1;
		
		// the first k elements (bottom-up��ģʽ)
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