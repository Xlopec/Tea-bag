import SwiftUI
import SharedAppLib
import Kingfisher


let article = Article(
    url: Url(impl: URL(string: "https://www.google.com")!),
    title: Title(value: "Clubhouse is now available for IOS"),
    author: Author(value: "Max Oliynick"),
    description: Description(value: """
Let your imagination fly! Modifiers let you modify your composable in a very flexible way. For example, if you wanted to add some outer spacing, change the background color of the composable, and round the corners of the Row, you could use the following code
"""),
    urlToImage: Url.init(impl: URL(string: "https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")!),
    published: CommonDate(impl: Date()),
    isFavorite: true
)

let articles = [article, article, article]


struct ArticlesView: View {
    
    let articles: [Article]
    
    init(articles: [Article]) {
        self.articles = articles
    }
    
    var body: some View {
        List(articles, id: \.url) { article in
            RowItem(article: article)
        }
    }
}

struct RowItem: View {
    
    private let dateFormatter = DateFormatter()
    
    let article: Article

    init(article: Article) {
        dateFormatter.dateFormat = "dd MMM' at 'hh:mm"
        self.article = article
    }
    
    var body: some View {
        VStack(alignment: HorizontalAlignment.leading, spacing: /*@START_MENU_TOKEN@*/nil/*@END_MENU_TOKEN@*/, content: {
            
            if let image = article.urlToImage?.impl {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(width: .infinity, height: 200, alignment: Alignment.center)
            }
            
            Text(article.title.value)
            
            if let author = article.author?.value {
                Text("By \(author)")
                    .font(.caption)
            }
            
            if let description = article.component4()?.value {
                Text(description).font(.subheadline).lineLimit(100)
            }
            
            Text("Published on \(dateFormatter.string(from: article.published.impl))")
                .font(.caption)
        })/*.frame(
            minWidth: 0,
            maxWidth: .infinity,
            minHeight: 0,
            maxHeight: 0,
            alignment: .leading
        )*/.padding(
            EdgeInsets(
                top: CGFloat(16.0),
                leading: CGFloat(16.0),
                bottom: CGFloat(16.0),
                trailing: CGFloat(16.0)
            )
        )
    }
}



struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        RowItem(article: article)
    }
}
